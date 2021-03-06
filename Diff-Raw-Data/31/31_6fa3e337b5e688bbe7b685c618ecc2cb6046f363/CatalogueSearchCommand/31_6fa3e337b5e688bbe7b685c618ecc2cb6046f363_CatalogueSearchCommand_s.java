 /*
  * Copyright (2005-2007) Schibsted Søk AS
  */
 package no.schibstedsok.searchportal.mode.command;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import no.schibstedsok.searchportal.mode.command.AbstractSearchCommand.ReconstructedQuery;
 import no.schibstedsok.searchportal.mode.config.CatalogueSearchConfiguration;
 import no.schibstedsok.searchportal.query.AndClause;
 import no.schibstedsok.searchportal.query.AndNotClause;
 import no.schibstedsok.searchportal.query.Clause;
 import no.schibstedsok.searchportal.query.DefaultOperatorClause;
 import no.schibstedsok.searchportal.query.LeafClause;
 import no.schibstedsok.searchportal.query.NotClause;
 import no.schibstedsok.searchportal.query.OperationClause;
 import no.schibstedsok.searchportal.query.OrClause;
 import no.schibstedsok.searchportal.query.PhraseClause;
 import no.schibstedsok.searchportal.query.Query;
 import no.schibstedsok.searchportal.query.XorClause;
 import no.schibstedsok.searchportal.query.finder.Counter;
 import no.schibstedsok.searchportal.query.finder.ParentFinder;
 import no.schibstedsok.searchportal.query.finder.PredicateFinder;
 import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter;
 import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.Application;
 import no.schibstedsok.searchportal.query.finder.WhoWhereSplitter.WhoWhereSplit;
 import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
 import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
 import no.schibstedsok.searchportal.query.token.TokenPredicate;
 import no.schibstedsok.searchportal.result.BasicSearchResultItem;
 import no.schibstedsok.searchportal.result.CatalogueSearchResultItem;
 import no.schibstedsok.searchportal.result.SearchResult;
 
 import org.apache.log4j.Logger;
 
 /**
  * The CatalogueSearchCommand is responsible for the query to search for
  * companies in the Katalog-website. It is executed for enrichment on sesam.no
  * and to retrieve the company info page.
  *
  * The search command uses a second query parameter to specify geographic
  * locations by the user. This geographic locations are parsed and an extra
  * query is created and appended to the default query. The parsing of
  * the geographic query is done by the inner class GeoVisitor in this file.
  *
  * By setting parameters in the modes.xml file, it is possible to specify
  * that the search command should analyze the q-parameter query content and
  * move recognized geographic locations to the geographic query part.
  *
  * The following attributes is configurable for this command in the modes.xml:
  *
  * split="true/false"
  * query-parameter-where="where"
  *
  *
  * The functionality in this class is enhanced by several QueryTransformers;
  *
  * @see no.schibstedsok.searchportal.query.transform.CatalogueExactTitleMatchQueryTransformer
  * @see no.schibstedsok.searchportal.query.transform.CatalogueEmptyQueryTransformer
  * @see no.schibstedsok.searchportal.query.transform.CatalogueInfopageQueryTransformer
  *
  *
  * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
  * @version $Id$
  */
 public final class CatalogueSearchCommand extends AdvancedFastSearchCommand {
 
     /** Logger for this class. */
     private static final Logger LOG = Logger.getLogger(CatalogueSearchCommand.class);
 
     /** User supplied query for geographic locations. */
     private String queryGeoString = null;
 
     /** User supplied value for sorting type of search result. */
     private String userSortBy = "kw"; // default er sorting på keywords
 
     /** The number of terms (words) in the largest COMPANY_KEYWORD_RESERVED match in the query.
      * Any leaf clauses within this match are boundary matched in the lemiypcfkeywords filter,
      *  all other leaf clauses matches within COMPANY_KEYWORD_RESERVED will be treated as normal leaves.
      **/
     private int keywordReservedTermSize = 0;
 
     /**
      * The largest existing clause matching COMPANY_KEYWORD_RESERVED in the query.
      * null if keywordReservedTermSize <= 0
      **/
     private transient Clause longestCkr;
 
     private transient Query whoQuery;
     private transient TokenEvaluationEngine whoEngine;
     private transient String whereString = "";
 
     /**
      *  Log strings for this class.
      */
     private static final String DEBUG_CONF_NFO    = "CatalogueSearchCommand Conf details->";
     private static final String DEBUG_SEARCHING_1 = "Catalogue Searching for who->";
     private static final String DEBUG_SEARCHING_2 = "Catalogue Searching for where->";
     private static final String DEBUG_SEARCHING_3 = "Catalogue Searching for geo->";
 
     /**
      *  Possible values to use as sort by in this search command.
      */
     private static final String SORTBY_COMPANYNAME = "iyprpnavn";
     private static final String SORTBY_KEYWORD = "iyprpkw";
 
     /**
      *  The names of the parameters where the result of the geographic split
      *  procedure is stored.
      */
     private static final String PARAMETER_NAME_WHAT = "catalogueWhat";
     private static final String PARAMETER_NAME_WHERE = "catalogueWhere";
 
     /**
      *  Represents a blank value in the query.
      */
     private static final String BLANK = "";
 
     /**
      * Creates a new catalogue search command.
      * @param cxt current context for this search command.
      */
     public CatalogueSearchCommand(final Context cxt) {
 
         super(cxt);
 
         final CatalogueSearchConfiguration conf = (CatalogueSearchConfiguration) cxt.getSearchConfiguration();
 
         LOG.debug(DEBUG_CONF_NFO + conf.getSearchBy() + ' '
                 + conf.getQueryParameterWhere() + ' '
                 + conf.getSplit());
 
         final WhoWhereSplit whoWhere = initialiseWhoWhere();
         initialiseWhoQuery(whoWhere);
         initialiseWhereQuery(whoWhere);
 
         // user may specify sorting in two different ways from the GUI,
         // by company name or by keyword. Default is by keyword.
         if (getSingleParameter("userSortBy") != null
                 && getSingleParameter("userSortBy").length() > 0
                 && getSingleParameter("userSortBy").equals("name")) {
 
             userSortBy = "name";
         } else {
             userSortBy = "kw";
         }
     }
 
 
     /**
      *  Do the split on recogniced known geographic locations, if specified
      *  in modes.xml, or create a WhoWhereSplit from the url's corresponding parameters q & where.
      *
      *  Put the result into two different attributes, catalogueWhat and
      *  catalogueWhere to be used in the frontend.
      *
      *  If the split results in empty Who and Where from split, use the
      *  original untransformed query.
      *
      *  Populate the knownGeo and knownGeoString which is used by the visitXxx
     *  methods to known which terms to ignore when constructing the
      *  query for this searchcommand.
      */
     private WhoWhereSplit initialiseWhoWhere() {
 
         final CatalogueSearchConfiguration conf = (CatalogueSearchConfiguration) context.getSearchConfiguration();
 
         WhoWhereSplit splitQuery = new WhoWhereSplit(
                 datamodel.getQuery().getString(),
                 getSingleParameter(conf.getQueryParameterWhere()));
 
         if(conf.getSplit()){
 
             // this will overwrite anything in the where parameter
             final WhoWhereSplitter splitter = new WhoWhereSplitter(new WhoWhereSplitter.Context(){
                 private final List<Application> applications = Arrays.asList(Application.YELLOW);
                 public Map<Clause,String> getTransformedTerms(){
                     return CatalogueSearchCommand.this.getTransformedTerms();
                 }
                 public Query getQuery() {
                     return datamodel.getQuery().getQuery();
                 }
                 public List<Application> getApplications() {
                     return applications;
                 }
             });
 
             final WhoWhereSplit ww = splitter.getWhoWhereSplit();
 
             if(null != ww.getWho() && ww.getWho().length() > 0 && null != ww.getWhere() && ww.getWhere().length() > 0){
 
                 splitQuery = ww;
             }
 
             LOG.debug(DEBUG_SEARCHING_1 + splitQuery.getWho());
             LOG.debug(DEBUG_SEARCHING_2 + splitQuery.getWhere());
         }
 
         return splitQuery;
     }
 
     /**
      *  Initialise the who query.
      */
     private void initialiseWhoQuery(final WhoWhereSplit whoWhereSplit){
 
         if(datamodel.getQuery().getString().equals(whoWhereSplit.getWho())){
 
             whoQuery = datamodel.getQuery().getQuery();
             whoEngine = context.getTokenEvaluationEngine();
 
         }else{
 
             // It's not the query we are looking for but a string held in a different parameter.
             final ReconstructedQuery recon = createQuery(whoWhereSplit.getWho());
 
             whoQuery = recon.getQuery();
             whoEngine = recon.getEngine();
         }
     }
 
     @Override
     protected Query getQuery(){
 
         return whoQuery;
     }
 
     @Override
     protected TokenEvaluationEngine getEngine(){
 
         return whoEngine;
     }
 
     /**
      *  Initialise the geographic query.
      */
     private void initialiseWhereQuery(final WhoWhereSplit whoWhereSplit) {
 
         if(null != whoWhereSplit.getWhere() && whoWhereSplit.getWhere().length() > 0){
 
             final ReconstructedQuery queryGeo = createQuery(whoWhereSplit.getWhere());
 
             final GeoVisitor geoVisitor = new GeoVisitor();
             geoVisitor.visit(queryGeo.getQuery().getRootClause());
             queryGeoString = geoVisitor.getQueryRepresentation();
             whereString = whoWhereSplit.getWhere();
         }
 
 
         LOG.info(DEBUG_SEARCHING_3 + queryGeoString);
     }
 
     /**
      *  Execute the search command query.
      *  Collect the result and copy the values over to our type of result
      *  objects. The search result, may be enriched by our resulthandler
      *  if specified in the modes.xml, this is only done in the Info Page
      *  search result.
      * @return the search result found by the executed query.
      */
     public SearchResult execute() {
 
         final SearchResult result = super.execute();
 
         final List<CatalogueSearchResultItem> nyResultListe = new ArrayList<CatalogueSearchResultItem>();
 
         for (Iterator iter = result.getResults().listIterator(); iter.hasNext();) {
 
             final BasicSearchResultItem basicResultItem = (BasicSearchResultItem) iter.next();
 
             final CatalogueSearchResultItem resultItem = new CatalogueSearchResultItem();
 
             for (Object o : basicResultItem.getFieldNames()) {
                 final String s = (String) o;
                 final String v = basicResultItem.getField(s);
                 resultItem.addField(s, v);
             }
 
             nyResultListe.add(resultItem);
         }
 
         // clear the old BasicSearchResult, and add our new CatalogueSearchResult
         // to be used instead.
         result.getResults().clear();
         result.getResults().addAll(nyResultListe);
 
         // add the who and where fields (preferred over using them out of the junkyard)
         result.addField(PARAMETER_NAME_WHAT, getTransformedQuerySesamSyntax());
         result.addField(PARAMETER_NAME_WHERE, whereString);
 
         // XXX deprecated approach
         getParameters().put(PARAMETER_NAME_WHAT, getTransformedQuerySesamSyntax());
         getParameters().put(PARAMETER_NAME_WHERE, whereString);
 
 
         return result;
     }
 
     /**
      * Get the query string after all transformations has been executed on it.
      *
      * If user has supplied the geographic where parameter or the split query
      * procedure resulted in geographic query to be generated for the search,
      * add it to the query.
      * @return the query that is going to be executed.
      */
     @Override
     public String getTransformedQuery() {
 
         final StringBuilder query = new StringBuilder(super.getTransformedQuery());
 
         boolean hasQueryString= query != null && query.length() > 0;
         boolean hasGeoQueryString = queryGeoString != null && queryGeoString.length() > 0;
 
         // two possible paths, with both what and where in query,
         // or just where. If nothing in either, something is wrong.
         if (hasQueryString && hasGeoQueryString){
 
             // both
             query.append(") " + QL_AND + " (" + queryGeoString + ')');
             query.insert(0, '(');
 
         } else if (!hasQueryString && hasGeoQueryString) {
 
             // just where
             query.replace(0, query.length(), queryGeoString);
 
         } else if (hasQueryString && !hasGeoQueryString) {
 
             // just what,
             // dosent need to do anything with the query, should just leave
             // the query as it is after transformation.
 
         } else{
 
            // none of what and where, this should not be possible.
            // throw new IllegalStateException("Emty query strings, should not be possible.
            //  [Primary="+query+", Geo="+queryGeoString+"]");
         }
 
         return query.toString();
     }
 
     /**
      * Set what to sort the resultset by.
      * The possible value is sort by company name, or by keywords.
      *
      * This value is used by the call to Fast-servers, to specify
      * which rank-profile to sort by.
      *
      * The sorting may be altered if user has supplied the userSortBy
      * parameter.
      * @return the sorting to be used when executing the query.
      */
     @Override
     protected String getSortBy() {
        String sortBy = SORTBY_KEYWORD;
         if ("name".equalsIgnoreCase(userSortBy)) {
             sortBy = SORTBY_COMPANYNAME;
         }
         return sortBy;
     }
 
     /**
      * Create query syntax for a phrase term.
      * @param term the term to make query syntax for.
      * @return created phrase query fragment for one term.
      */
     private String createPhraseQuerySyntax(final String term) {
 
        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("iypcfnavn:" + term + " ANY ");
        sb.append("lemiypcfkeywords:" + term + " ANY ");
        sb.append("lemiypcfkeywordslow:" + term);
        sb.append(")");
        return sb.toString();
     }
 
     /**
      * Create the query syntax for one(1) search term.
      *
      * If the query is defined to split known geographic locations from
      * the terms and it is recognices as a known geographic location,
      * ignore the term.
      *
      * Check if there is any special characters in the query, if there
      * is, wrap term in " " characters and use none-phonetic composite field
      * in index for part of query.
      *
      * If the term is '', also ignore it.
      * @param clause the clause to process.
      */
     @Override
     protected void visitImpl(final LeafClause clause) {
 
         final Pattern p = Pattern.compile("\\.|\\-");
         final Matcher m = p.matcher(getTransformedTerms().get(clause));
 
         final boolean hasNotWordCharacters = m.find();
 
         if(hasNotWordCharacters){
 
             appendToQueryRepresentation(createPhraseQuerySyntax('\"' + getTransformedTerms().get(clause) + '\"'));
 
         }else if(!getTransformedTerms().get(clause).equals(BLANK)) {
 
             final Query query = context.getDataModel().getQuery().getQuery();
 
             final List<OperationClause> ancestors
                     = query.getParentFinder().getAncestors(query.getRootClause(), clause);
 
             if(0 == keywordReservedTermSize){
 
                 longestCkr = new PredicateFinder().findFirstClause(
                         query.getRootClause(),
                         TokenPredicate.COMPANY_KEYWORD_RESERVED,
                         getEngine());
 
                 keywordReservedTermSize = null != longestCkr
                         ? new Counter().getTermCount(longestCkr)
                         : -1;
             }
 
             boolean insideCKR = false;
             final Clause ckr = ancestors.size()>0? ancestors.get(0) : null;
 
             if(null != ckr){
                  insideCKR =
                     0 < keywordReservedTermSize
                     && new Counter().getTermCount(ckr) == keywordReservedTermSize
                     && ParentFinder.insideOf(ancestors, TokenPredicate.COMPANY_KEYWORD_RESERVED);
             }
 
 
             if(insideCKR){
 
                 // SEARCH-1796
                 if( ((OperationClause)longestCkr).getFirstClause() == clause ){
 
                     appendToQueryRepresentation(
                             "lemiypcfkeywords:\"^"
                             + ckr.getTerm().replaceAll("\\(|\\)", "")
                             +   "$\"");
                 }
 
             }else{
 
                 appendToQueryRepresentation(
                         '('
                         + "iypcfphnavn:" + getTransformedTerms().get(clause) + " ANY "
                         + "lemiypcfkeywords:" + getTransformedTerms().get(clause) + " ANY "
                         + "lemiypcfkeywordslow:" + getTransformedTerms().get(clause)
                         + ')');
             }
         }
     }
 
     /**
      * If the user has searched for a phrase, create the query syntax for
      * phrases in the query.
      *
      * If the query is defined to split known geographic locations from
      * the keywords, ignore the term.
      *
     * If the term is '*', also ignore it.
      * @param clause the clause to process.
      */
     protected void visitImpl(final PhraseClause clause) {
 
         if (!getTransformedTerms().get(clause).equals(BLANK)) {
 
             appendToQueryRepresentation(createPhraseQuerySyntax(getTransformedTerms().get(clause)));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void visitImpl(final DefaultOperatorClause clause) {
 
 
         clause.getFirstClause().accept(this);
         final int queryRepLength = getQueryRepresentationLength();
 
 
         clause.getSecondClause().accept(this);
 
         final boolean queryRepGrown = queryRepLength > 0 && getQueryRepresentationLength() > queryRepLength;
 
         if(queryRepGrown && !(clause.getSecondClause() instanceof NotClause)){
             // we know the query representation got longer which means we need to insert the operator
             insertToQueryRepresentation(queryRepLength, QL_AND);
         }
     }
 
     /**
      * Query builder for creating the geographic query.
      *
      */
     private static class GeoVisitor extends AbstractReflectionVisitor {
 
         /** the composite field in the index to search in. */
         private static final String GEO_COMPOSITE_FIELD_NAME = "iypcfgeo:";
 
         /** used while building the query. */
         private final StringBuilder sb = new StringBuilder();
 
         /**
          * Returns the generated query.
          *
          * @return The query.
          */
         String getQueryRepresentation() {
             return sb.toString();
         }
 
         protected void visitImpl(final LeafClause clause) {
             if (clause.getTerm() != null && clause.getTerm().length() > 0) {
                 sb.append(GEO_COMPOSITE_FIELD_NAME + clause.getTerm());
             }
         }
 
         protected void visitImpl(final AndClause clause) {
             clause.getFirstClause().accept(this);
             if (!(clause.getSecondClause() instanceof NotClause)) {
                 sb.append(QL_AND);
             }
             clause.getSecondClause().accept(this);
         }
 
         protected void visitImpl(final OrClause clause) {
             sb.append("(");
             clause.getFirstClause().accept(this);
 
             sb.append(QL_OR);
 
             clause.getSecondClause().accept(this);
             sb.append(")");
         }
 
         protected void visitImpl(final DefaultOperatorClause clause) {
             clause.getFirstClause().accept(this);
             if (!(clause.getSecondClause() instanceof NotClause)) {
                 sb.append(QL_AND);
             }
             clause.getSecondClause().accept(this);
         }
 
         protected void visitImpl(final NotClause clause) {
 
             final String childsTerm = clause.getFirstClause().getTerm();
             if (childsTerm != null && childsTerm.length() > 0) {
                 sb.append(QL_ANDNOT);
                 clause.getFirstClause().accept(this);
             }
         }
 
         protected void visitImpl(final AndNotClause clause) {
             // the first term can not be ANDNOT term.
             if(sb.toString().trim().length()>0){
                 sb.append(QL_ANDNOT);
                 clause.getFirstClause().accept(this);
             }
         }
 
         protected void visitImpl(final XorClause clause){
             clause.getFirstClause().accept(this);
         }
     }
 }
