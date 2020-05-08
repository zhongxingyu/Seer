 package org.esupportail.opi.dao;
 
 
 import com.mysema.query.Tuple;
 import com.mysema.query.group.Group;
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.hibernate.HibernateQuery;
 import com.mysema.query.types.EntityPath;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.mysema.query.types.path.EntityPathBase;
 import com.mysema.query.types.path.PathBuilder;
 import com.mysema.query.types.path.SetPath;
 import com.mysema.query.types.template.BooleanTemplate;
 import fj.F;
 import fj.F2;
 import fj.Function;
 import fj.P2;
 import fj.data.Option;
 import fj.data.Stream;
 import org.esupportail.opi.dao.utils.Paginator;
 import org.esupportail.opi.dao.utils.PaginatorFactory;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.parameters.TypeTraitement;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.utils.primefaces.PFFilters;
 import org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper;
 import org.springframework.orm.hibernate3.HibernateTransactionManager;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import java.util.*;
 
 import static com.mysema.query.group.GroupBy.groupBy;
 import static com.mysema.query.group.GroupBy.set;
 import static fj.P.p;
 import static fj.data.Array.iterableArray;
 import static fj.data.IterableW.wrap;
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
 
     final EntityPath<IndVoeu> indVoeuBase = new EntityPathBase<>(IndVoeu.class, "voeux");
     final PathBuilder<IndVoeu> indVoeu = new PathBuilder<>(IndVoeu.class, indVoeuBase.getMetadata());
     final PathBuilder<LinkTrtCmiCamp> linkTrtCmiCamp = indVoeu.get("linkTrtCmiCamp", LinkTrtCmiCamp.class);
     final PathBuilder<TraitementCmi> trtCmi = linkTrtCmiCamp.get("traitementCmi", TraitementCmi.class);
     final SetPath<IndVoeu, PathBuilder<IndVoeu>> indVoeux;
 
     final EntityPath<Avis> avisBase = new EntityPathBase<>(Avis.class, "avis");
     final PathBuilder<Avis> avis = new PathBuilder<>(Avis.class, avisBase.getMetadata());
     final SetPath<Avis, PathBuilder<Avis>> indVoeuAvis;
     final BooleanExpression avisEnServ;
 
     final BooleanExpression oneIsOne = BooleanTemplate.create("1 = 1");
 
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
 
     private JPQLQuery from(EntityPath<?>... ind) {
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
                             return typeTrtCmis.size() > 0 ?
                                     subExpr.and(trtCmi.get("codTypeTrait").in(typeTrtCmis)) :
                                     subExpr.and(trtCmi.isNull());
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
 
 
     final F<Collection<TypeTraitement>, F<BooleanExpression, BooleanExpression>> isCodeTypeTrtmtFilter =
             new F<Collection<TypeTraitement>, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final Collection<TypeTraitement> typeTraitements) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression expr) {
                             return expr.and(indVoeu.get("codTypeTrait").in(iterableArray(typeTraitements)
                                     .map(new F<TypeTraitement, String>() {
                                         public String f(TypeTraitement typeTraitement) {
                                             return typeTraitement.getCode();
                                         }
                                     })
                                     .toCollection()));
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
                         	Calendar cTo = Calendar.getInstance();
                         	cTo.setTime(date);
                         	cTo.set(Calendar.HOUR_OF_DAY, 23);
                         	cTo.set(Calendar.MINUTE, 59);
                         	cTo.set(Calendar.SECOND, 59);
                         	cTo.set(Calendar.MILLISECOND, 999);
                         	return expr.and(indVoeu.getDate("dateCreaEnr", Date.class).between(date, cTo.getTime()));
                         }
                     };
                 }
             };
 
 
     @Override
     public P2<Long, Stream<Individu>> sliceOfInds(final PFFilters pfFilters,
                                                   final List<TypeDecision> typesDec,
                                                   final Option<Boolean> treatedWish,
                                                   final Option<Boolean> validWish,
                                                   final Option<Date> wishCreation,
                                                   final Collection<TypeTraitement> typeTrtmts,
                                                   final Option<Set<TraitementCmi>> trtCmis,
                                                   final Set<Integer> listCodesRI,
                                                   final Option<List<String>> typesTrtVet) {
 
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
                         iif(!typeTrtmts.isEmpty(), isCodeTypeTrtmtFilter.f(typeTrtmts)),
                         wishCreation.map(wishCreationFilter)
                 )).foldLeft(Function.<BooleanExpression, BooleanExpression, BooleanExpression>andThen(),
                         Function.<BooleanExpression>identity());
 
         final F<HibernateQuery, HibernateQuery> customFilterQuery = new F<HibernateQuery, HibernateQuery>() {
             public HibernateQuery f(HibernateQuery query) {
             	return query.distinct()
                         .leftJoin(indVoeux, indVoeu)
                         .innerJoin(indCamps, camp)
                         .leftJoin(indVoeuAvis, avis)
                         .where(customCampFilter.andThen(customVoeuFilter).f(oneIsOne));
             }
         };
 
         return pagInd.sliceOf(
                 pfFilters.first,
                 pfFilters.pageSize,
                 pfFilters.sortField,
                 pfFilters.sortOrder,
                 pfFilters.filters,
                 some(customFilterQuery),
                 new F2<EntityPathBase<Individu>, HibernateQuery, Stream<Individu>>() {
                     public Stream<Individu> f(EntityPathBase<Individu> ent, HibernateQuery query) {
                        //TODO sorry a hack of https://groups.google.com/forum/#!topic/querydsl/O3xXFFqeYuw check to make it elegant,
                        if (trtCmis.map(trtCmiFilter).isNone()){
                            return iterableStream(query.list(ent));
                        }else {
                            final Map<String, Group> transform = query.transform(groupBy(ind.getString("numDossierOpi"))
                                    .as(ind, set(indVoeu)));
                            return iterableStream(transform.keySet())
                                    .map(new F<String, Individu>() {
                                        @Override
                                        public Individu f(final String numDossierOpi) {
                                            Group group = transform.get(numDossierOpi);
                                            final Individu individu = group.getOne(ind);
                                            final Set<IndVoeu> voeux = group.getSet(indVoeu);
                                            individu.setVoeux(voeux);
                                            return individu;
                                        }
                                    });
                        }
                     }
 
 
                 });
     }
 
     private static final F<BooleanExpression, F<BooleanExpression,BooleanExpression>> and =
             new F<BooleanExpression, F<BooleanExpression, BooleanExpression>>() {
                 public F<BooleanExpression, BooleanExpression> f(final BooleanExpression b1) {
                     return new F<BooleanExpression, BooleanExpression>() {
                         public BooleanExpression f(BooleanExpression b2) {
                             return b1.and(b2);
                         }
                     };
                 }
             };
 
     @Override
     public List<String> getIndsIds(final Commission commission, final Boolean validate, final Set<Integer> listeRICodes) {
         final F<BooleanExpression, BooleanExpression> filter =
                 somes(list(
                         some(p(indEnService).<BooleanExpression>constant()),
                         some(commission.getTraitementCmi()).map(trtCmiFilter),
                         fromNull(listeRICodes).map(and.o(campRiFilter)),
                         fromNull(validate).map(validWishFilter),
                         iif(validate != null, and.f(avisEnServ))
                 )).foldLeft(Function.<BooleanExpression, BooleanExpression, BooleanExpression>andThen(),
                         Function.<BooleanExpression>identity());
 
         final List<Tuple> numsDossierOpi =
                 from(indEnt).distinct()
                         .leftJoin(indVoeux, indVoeu)
                         .innerJoin(indCamps, camp)
                         .leftJoin(indVoeuAvis, avis)
                         .where(filter.f(BooleanTemplate.TRUE))
                         .orderBy(ind.getString("nomPatronymique").asc())
                         .list(ind.getString("nomPatronymique"), ind.getString("numDossierOpi"));
 
         return wrap(numsDossierOpi).map(new F<Tuple, String>() {
             public String f(Tuple tuple) {
                 return tuple.get(1, String.class);
             }
         }).toStandardList();
     }
 
     @Override
     public Individu fetchIndById(String id, Option<Boolean> onlyValidWishes) {
         final Map<Individu, Set<IndVoeu>> results =
                 from(indEnt)
                         .leftJoin(indVoeux, indVoeu)
                         .leftJoin(indVoeuAvis, avis)
                         .where(ind.getString("numDossierOpi").eq(id)
                                 .and(onlyValidWishes
                                         .option(p(oneIsOne).<BooleanExpression>constant(), validWishFilter)
                                         .f(oneIsOne)))
                         .transform(groupBy(indEnt).as(set(indVoeu)));
 
         final Individu individu = results.keySet().iterator().next();
         final Set<IndVoeu> voeux = results.get(individu);
 
         // Gros hack dégoûtant (mais efficace, en tout cas plus que du fetch/join)
         // pour forcer le chargement des collections par hibernate
         for (IndVoeu v : voeux) {
             LinkTrtCmiCamp link = v.getLinkTrtCmiCamp();
             TraitementCmi trt = link.getTraitementCmi();
             link.toString();
             trt.toString();
         }
 
         individu.setVoeux(voeux);
         return individu;
     }
 }
