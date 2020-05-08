 package org.esupportail.opi.batch;
 
 import java.util.List;
 import java.util.Set;
 
 import org.esupportail.commons.context.ApplicationContextHolder;
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.services.application.ApplicationService;
 import org.esupportail.commons.services.application.ApplicationUtils;
 import org.esupportail.commons.services.database.DatabaseUtils;
 import org.esupportail.commons.services.exceptionHandling.ExceptionUtils;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 
 import org.esupportail.opi.domain.DomainService;
 import org.esupportail.opi.domain.ParameterService;
 import org.esupportail.opi.domain.beans.parameters.InscriptionAdm;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 
import static org.esupportail.opi.domain.beans.parameters.TypeTraitement.*;

 
 /**
  * @author cleprous
  * Ce batch permet de recuperer tous les veoux des types transfert et leur mettre un avis favorable.
  * Ceci a ete fait car dans le lot1 l"ajout de l'avis n'etait pas fait. Il existe donc de voeu transfert sans avis.
  */
 public class UpdateTransfertInd  {
 
 	/**
 	 * A logger.
 	 */
 	private static final Logger LOG = new LoggerImpl(UpdateTransfertInd.class);
 
 
 	/**
 	 * Bean constructor.
 	 */
 	private UpdateTransfertInd() {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * update wishes of type transfert. 
 	 */
 	private static void updateTransfert() {
 		DomainService domainService = (DomainService) ApplicationContextHolder.getContext().getBean("domainService");
 		try {
 
 			DatabaseUtils.open();
 			DatabaseUtils.begin();
			List<Individu> individus = domainService.getIndividusWishes(null, Transfert.code);
 			TypeDecision favorable = getFavorable();
 			if (favorable == null) {
 				throw new ConfigException(
 						"Il n'y pas de type de decision menant e l'inscription administrative");
 			}
 			int cpt = 0;
 			LOG.info(cpt + " avis vient d'etre ajoute.");
 			for (Individu i : individus) {
 				//on regarde les voeux en trasfert
 				for (IndVoeu iVoeu : i.getVoeux()) {
					if (iVoeu.getCodTypeTrait().equals(Transfert.code)) {
 						if (iVoeu.getAvis() == null 
 								|| iVoeu.getAvis().isEmpty()) {
 							LOG.info("add avis for iVoeu = " + iVoeu);
 							//si pas d'avis on lui met un avis favorable valide
 							Avis a = new Avis();
 							a.setTemoinEnService(true);
 							a.setValidation(true);
 							a.setResult(favorable);
 							a.setIndVoeu(iVoeu);
 							Avis av = (Avis) domainService.add(
 									a, "BATCH_CORRECTION_TYPE_TRANSFERT");
 							domainService.addAvis(av);
 							cpt++;
 						}
 					}
 				}
 			}
 			LOG.info(cpt + " avis vient d'etre ajoute.");
 			DatabaseUtils.commit();
 
 		} catch (Exception e) {
 			DatabaseUtils.rollback();
 		} finally {
 			DatabaseUtils.close();
 		}
 	}
 
 
 	private static TypeDecision getFavorable() {
 		ParameterService parameterService = (ParameterService) ApplicationContextHolder.getContext().getBean("parameterService");
 		InscriptionAdm inscriptionAdm = (InscriptionAdm) ApplicationContextHolder.getContext().getBean("inscriptionAdm");
 		Set<TypeDecision> dec = parameterService.getTypeDecisions(true);
 		for (TypeDecision d : dec) {
 			if (d.getIsFinal() && d.getCodeTypeConvocation().equals(inscriptionAdm.getCode())) {
 				return d;
 			}
 		}
 		return null;
 	}
 
 
 
 
 	/**
 	 * Print the syntax and exit.
 	 */
 	private static void syntax() {
 		throw new IllegalArgumentException(
 				"syntax: " + UpdateTransfertInd.class.getSimpleName() + " <options>"
 				+ "\nwhere option can be:"
 				+ "\n- test-beans: test the required beans");
 	}
 
 
 
 	/**
 	 * Dispatch dependaing on the arguments.
 	 * @param args
 	 */
 	protected static void dispatch(final String[] args) {
 		switch (args.length) {
 		case 0:
 			updateTransfert();
 			break;
 		default:
 			syntax();
 		break;
 		}
 	}
 
 	/**
 	 * The main method, called by ant.
 	 * @param args
 	 */
 	public static void main(final String[] args) {
 		try {
 			ApplicationService applicationService = ApplicationUtils.createApplicationService();
 			LOG.info(applicationService.getName() + " v" + applicationService.getVersion());
 			dispatch(args);
 		} catch (Throwable t) {
 			ExceptionUtils.catchException(t);
 		}
 	}
 
 }
