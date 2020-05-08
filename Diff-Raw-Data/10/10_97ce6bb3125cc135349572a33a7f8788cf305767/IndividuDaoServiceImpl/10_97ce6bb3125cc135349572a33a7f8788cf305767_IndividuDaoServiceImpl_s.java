 package org.esupportail.opi.dao;
 
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.hibernate.HibernateQuery;
 import com.mysema.query.types.EntityPath;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.mysema.query.types.path.EntityPathBase;
 import com.mysema.query.types.path.PathBuilder;
 import com.mysema.query.types.path.SetPath;
 import com.mysema.query.types.template.BooleanTemplate;
 import fj.F;
 import fj.Function;
 import fj.P2;
 import fj.data.Option;
 import fj.data.Stream;
 import org.esupportail.opi.dao.utils.Paginator;
 import org.esupportail.opi.dao.utils.PaginatorFactory;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.utils.primefaces.PFFilters;
 import org.springframework.orm.hibernate3.HibernateTransactionManager;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import static fj.P.p;
 import static fj.data.List.list;
 import static fj.data.Option.*;
 import static fj.data.Stream.iterableStream;
 import static org.esupportail.opi.utils.fj.Conversions.parseBoolean;
 
 @SuppressWarnings("unchecked")
 public class IndividuDaoServiceImpl implements IndividuDaoService {
 
     final HibernateTransactionManager txm;
 
     final TransactionTemplate txTemplate;
 
     final PaginatorFactory pf;
     final Paginator<HibernateQuery, Individu> pagInd;
 
     final EntityPathBase<Individu> indEnt = new EntityPathBase<>(Individu.class, "ent");
     final PathBuilder<Individu> ind = new PathBuilder<>(Individu.class, indEnt.getMetadata());
     final BooleanExpression indEnService;
 
     final EntityPath<Campagne> campBase = new EntityPathBase<>(Campagne.class, "campagne");
     final PathBuilder<Campagne> camp = new PathBuilder<>(Campagne.class, campBase.getMetadata());
     final SetPath<Campagne, PathBuilder<Campagne>> indCamps;
 
     final EntityPath<IndVoeu> indVoeuBase = new EntityPathBase<>(IndVoeu.class, "voeu");
     final PathBuilder<IndVoeu> indVoeu = new PathBuilder<>(IndVoeu.class, indVoeuBase.getMetadata());
     final SetPath<IndVoeu, PathBuilder<IndVoeu>> indVoeux;
 
     final EntityPath<Avis> avisBase = new EntityPathBase<>(Avis.class, "avis");
     final PathBuilder<Avis> avis = new PathBuilder<>(Avis.class, avisBase.getMetadata());
     final SetPath<Avis, PathBuilder<Avis>> indVoeuAvis;
     final BooleanExpression avisEnServ;
 
 
     private IndividuDaoServiceImpl(PaginatorFactory pf, HibernateTransactionManager txm) {
         this.txm = txm;
         txTemplate = new TransactionTemplate(txm);
         this.pf = pf;
         pagInd = pf.indPaginator().withEntity(indEnt, ind);
         indCamps = ind.getSet("campagnes", Campagne.class);
         indVoeux = ind.getSet("voeux", IndVoeu.class);
         indVoeuAvis = indVoeu.getSet("avis", Avis.class);
         indEnService = ind.get("temoinEnService").eq(true);
         avisEnServ = avis.get("temoinEnService").eq(true);
     }
 
     public static IndividuDaoService individuDaoSrv(PaginatorFactory pf, HibernateTransactionManager txm) {
         return new IndividuDaoServiceImpl(pf, txm);
     }
 
     @SafeVarargs
     private final JPQLQuery from(EntityPath<Individu>... ind) {
         return new HibernateQuery(txm.getSessionFactory().getCurrentSession()).from(ind);
     }
 
     // ######## Filtres sur les campagnes de l'individu
 
     final BooleanExpression campEnService = camp.get("temoinEnService").eq(true);
 
     final F<Set<Integer>, BooleanExpression> campRiFilter = new F<Set<Integer>, BooleanExpression>() {
         public BooleanExpression f(Set<Integer> listCodesRI) {
             return camp.get("codeRI").in(listCodesRI);
         }
     };
 
     final F<Set<Integer>, F<BooleanExpression, BooleanExpression>> campTemoinAndRIFilter =
             new F<Set<Integer>, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Set<Integer> listCodesRI) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                             BooleanExpression subExpr = listCodesRI.size() > 0 ?
                                     campEnService.and(campRiFilter.f(listCodesRI)) :
                                     camp.isNull();
                             return expr.and(subExpr);
                         }
                     };
                 }
             };
 
     // ######## Filtres sur les voeux de l'individu
 
     final F<Boolean, F<BooleanExpression, BooleanExpression>> baseVoeuFilter =
             new F<Boolean, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Boolean bool) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                             return bool ?
                                     expr.and(indVoeu.get("individu").eq(ind.get("id"))) :
                                     expr.and(BooleanTemplate.create("1 = 1"));
 
                         }
                     };
                 }
             };
 
     final F<Set<TraitementCmi>, F<BooleanExpression, BooleanExpression>> trtCmiFilter =
             new F<Set<TraitementCmi>, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Set<TraitementCmi> trtCmis) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression subExpr) {
                             final PathBuilder<TraitementCmi> trtCmi = indVoeu
                                             .get("linkTrtCmiCamp", LinkTrtCmiCamp.class)
                                             .get("traitementCmi", TraitementCmi.class);
                             return trtCmis.size() > 0 ? subExpr.and(trtCmi.in(trtCmis)) : subExpr.and(trtCmi.isNull());
                         }
                     };
                 }
             };
 
     final F<List<String>, F<BooleanExpression, BooleanExpression>> typesTrtVetFilter =
             new F<List<String>, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final List<String> typeTrtCmis) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression subExpr) {
                             final PathBuilder<TraitementCmi> typeTrtCmi = indVoeu
                                             .get("linkTrtCmiCamp", LinkTrtCmiCamp.class)
                                             .get("traitementCmi", TraitementCmi.class);
                             return typeTrtCmis.size() > 0 ? subExpr.and(typeTrtCmi.get("codTypeTrait").in(typeTrtCmis)) : subExpr.and(typeTrtCmi.isNull());
                         }
                     };
                 }
             };            
             
     final F<List<TypeDecision>, F<BooleanExpression, BooleanExpression>> typeDecFilter =
             new F<List<TypeDecision>, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final List<TypeDecision> typesDecision) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression subExpr) {
                             PathBuilder<TypeDecision> typeDec = avis.get("result", TypeDecision.class);
                             return subExpr.and(avisEnServ).and(typeDec.in(typesDecision));
                         }
                     };
                 }
             };
 
     final F<Boolean, F<BooleanExpression, BooleanExpression>> unTreatedWishFilter =
             new F<Boolean, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Boolean excludeTreated) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                             return expr.and(indVoeu.get("haveBeTraited").eq(excludeTreated));
                         }
                     };
                 }
             };
 
 
     final F<String, F<BooleanExpression, BooleanExpression>> notCodeTypeTrtmtFilter =
             new F<String, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final String code) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                             return expr.and(indVoeu.get("codTypeTrait").ne(code));
                         }
                     };
                 }
             };
 
     final F<Boolean, F<BooleanExpression, BooleanExpression>> validWishFilter =
             new F<Boolean, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Boolean valid) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                             return expr.and(avis.get("validation", Boolean.class).eq(valid));
                         }
                     };
                 }
             };
 
     final F<Date, F<BooleanExpression, BooleanExpression>> wishCreationFilter =
             new F<Date, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Date date) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                            return expr.and(indVoeu.getDate("dateCreaEnr", Date.class).eq(date));
                         }
                     };
                 }
             };
 
 
 
     @Override
     public P2<Long, Stream<Individu>> sliceOfInd(final PFFilters pfFilters,
                                                  final List<TypeDecision> typesDec,
                                                  final Option<Boolean> treatedWish,
                                                  final Option<Boolean> validWish,
                                                  final Option<Date> wishCreation,
                                                  final Option<String> codeTypeTrtmt,
                                                  final Option<Set<TraitementCmi>> trtCmis,
                                                  final Set<Integer> listCodesRI,
                                                  final Option<List<String>> typesTrtVet) {
 
         final BooleanExpression andExpr = BooleanTemplate.create("1 = 1");
 
         final F<BooleanExpression, BooleanExpression> customCampFilter =
                 p(indEnService).<BooleanExpression>constant().andThen(campTemoinAndRIFilter.f(listCodesRI));
 
         final F<BooleanExpression, BooleanExpression> customVoeuFilter =
                 somes(list(
                         fromNull(pfFilters.filters.remove("useVoeuFilter"))
                                 .map(parseBoolean.andThen(baseVoeuFilter)),
                         trtCmis.map(trtCmiFilter),
                         iif(!typesDec.isEmpty(), typeDecFilter.f(typesDec)),
                         typesTrtVet.map(typesTrtVetFilter),
                         validWish.map(validWishFilter),
                         treatedWish.map(unTreatedWishFilter),
                         codeTypeTrtmt.map(notCodeTypeTrtmtFilter),
                         wishCreation.map(wishCreationFilter)
                 )).foldLeft(Function.<BooleanExpression, BooleanExpression, BooleanExpression>andThen(),
                         Function.<BooleanExpression>identity());
 
         final F<HibernateQuery, HibernateQuery> customFilterQuery = new F<HibernateQuery, HibernateQuery>() {
             public HibernateQuery f(HibernateQuery query) {
             	return query.distinct()
                         .leftJoin(indVoeux, indVoeu)
                         .innerJoin(indCamps, camp)
                         .leftJoin(indVoeuAvis, avis)
                         .where(customCampFilter.andThen(customVoeuFilter).f(andExpr));
             }
         };
 
         return pagInd.lazySliceOf(
                 pfFilters.first,
                 pfFilters.pageSize,
                 pfFilters.sortField,
                 pfFilters.sortOrder,
                 pfFilters.filters,
                 some(customFilterQuery));
     }
 
     @Override
     public Stream<Individu> getIndividus(final Commission commission,
                                          final Boolean validate,
                                          final Set<Integer> listeRICodes) {
         final F<BooleanExpression, F<BooleanExpression, BooleanExpression>> constant = Function.constant();
 
         final F<BooleanExpression, BooleanExpression> filter =
                 somes(list(
                         some(constant.f(indEnService)),
                         some(commission.getTraitementCmi()).map(trtCmiFilter),
                         fromNull(listeRICodes).map(campRiFilter.andThen(constant)),
                         fromNull(validate).map(validWishFilter),
                         iif(validate != null, constant.f(avisEnServ))
                 )).foldLeft(Function.<BooleanExpression, BooleanExpression, BooleanExpression>andThen(),
                         Function.<BooleanExpression>identity());
 
         final List<Object[]> numsDossierOpi =
                 from(indEnt).distinct()
                         .leftJoin(indVoeux, indVoeu)
                         .innerJoin(indCamps, camp)
                         .leftJoin(indVoeuAvis, avis)
                         .where(filter.f(BooleanTemplate.TRUE))
                         .orderBy(ind.getString("nomPatronymique").asc())
                         .list(ind.getString("nomPatronymique"), ind.getString("numDossierOpi"));
 
         final F<Object[], Individu> getInd = new F<Object[], Individu>() {
             public Individu f(Object[] p2) {
                 return from(indEnt).where(ind.getString("numDossierOpi").eq((String) p2[1])).uniqueResult(indEnt);
             }
         };
 
         return iterableStream(numsDossierOpi).map(getInd);
     }
 
 }
