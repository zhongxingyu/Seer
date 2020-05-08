 package plan;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import models.Cacho;
 import models.UserCacho;
 import models.UserCachos;
 import models.Video;
 import play.Play;
 
 public class CachoAjenoMaker {
 
 	private final Video video;
 	private int mb = 1024*1024;
 	private int firstCachoSize = Integer.valueOf(Play.configuration.getProperty("first.cacho.size")) * mb;
 	private int maxCachoSize = Integer.valueOf(Play.configuration.getProperty("max.cacho.size")) * mb; 
 	private final List<Cacho> cachosQueFaltan;
 	private final List<UserCachos> noPlanRequesterCachos;
 	private final List<UserCacho> result = new ArrayList<UserCacho>();
 
 	public CachoAjenoMaker(Video video, List<Cacho> cachosQueFaltan, List<UserCachos> noPlanRequesterCachos) {
 		this.video = video;
 		this.cachosQueFaltan = cachosQueFaltan;
 		this.noPlanRequesterCachos = noPlanRequesterCachos;
 	}
 
 	public List<UserCacho> getCachos() {
 		/*
 		 * asumo que puedo crear los cachos
 		 */
 		return result;
 	}
 
 
 	public boolean canMakeCachos() {
 		/*
 		 * necesito una copia de la lista para ir eliminando en el round robin
 		 */
 		List<UserCachos> tempNoPlanRequesterCachos = new ArrayList<UserCachos>(noPlanRequesterCachos);
 
 		for(Cacho cacho : cachosQueFaltan){
 			List<UserCacho> cachosCandidatos = cachosCandidatos(cacho, tempNoPlanRequesterCachos);
 			if(cachosCandidatos.isEmpty()){
 				/*
 				 * reintento con todos, no con los temp
 				 */
 				cachosCandidatos = cachosCandidatos(cacho, noPlanRequesterCachos);
 				if(cachosCandidatos.isEmpty()){
 					/*
 					 * no puedo hacer el plan
 					 */
 					return false;
 				}
 			}
 
 			List<UserCacho> cachosRecortados = cutCacho(cacho, cachosCandidatos);
 			result.addAll(cachosRecortados);
 
 			cleanForRoundRobin(cachosCandidatos, tempNoPlanRequesterCachos);
 		}
 
 		return true;
 	}
 
 	private void cleanForRoundRobin(List<UserCacho> cachosCandidatos,
 			List<UserCachos> tempNoPlanRequesterCachos) {
 
 		List<UserCachos> aBorrar = new ArrayList<UserCachos>();
 
 		for(UserCacho userCacho : cachosCandidatos){
 			for(UserCachos uc : tempNoPlanRequesterCachos){
 				if(uc.user.email.equals(userCacho.getUser().email)){
 					aBorrar.add(uc);
 				}
 			}
 		}
 
 		tempNoPlanRequesterCachos.removeAll(aBorrar);
 	}
 
 	/**
 	 * 
 	 * @param cacho
 	 * @param cachosCandidatos
 	 * @return la lista de userCachos que conforman el cacho requerido, segun los limites de tamaño configurados
 	 */
 	private List<UserCacho> cutCacho(Cacho cacho, List<UserCacho> cachosCandidatos) {
 
 		List<UserCacho> result = new ArrayList<UserCacho>();
 		Cacho currentCachoTarget = new Cacho(cacho.start, cacho.lenght);
 		UserCacho lastAdded = null;
 
 
 		while(otroCachoNeeded(currentCachoTarget, lastAdded) ) {

			if(cachosCandidatos.size() == 1){
				// le pido el cacho al mismo Pir porque no hay mas
				UserCacho userCacho = cachosCandidatos.get(0);
				UserCacho uc = cutCacho(currentCachoTarget, userCacho);
				result.add(uc);
				lastAdded = uc;

			} else {

 				for(UserCacho userCacho : cachosCandidatos){
 					UserCacho uc = cutCacho(currentCachoTarget, userCacho);
 					result.add(uc);
 					lastAdded = uc;
 
					if(!otroCachoNeeded(currentCachoTarget, lastAdded)){
 						break;
 					}
 				}
			}

 		}
 		return result;
 	}
 
 
 	private boolean otroCachoNeeded(Cacho cacho, UserCacho uc) {
 
 		if(uc == null) return true;
 
 		if(uc.getCacho().lastByte() != cacho.lastByte()){
 			// necesito otro cacho
 			long nextCachoFrom = uc.getCacho().start + uc.getCacho().lenght; 
 			long nextCachoLenght = (cacho.start + cacho.lenght) - nextCachoFrom;
 			cacho.start = nextCachoFrom;
 			cacho.lenght = nextCachoLenght;
 			return true;
 		}
 		return false;
 	}
 
 	private UserCacho cutCacho(Cacho cacho, UserCacho cachoCandidato) {
 
 		boolean firstCacho = cacho.start == 0;
 		long cachoSize = firstCacho ? firstCachoSize : maxCachoSize;
 		long cachoLenght = cacho.lenght > cachoSize ? cachoSize : cacho.lenght;
 
 		return new UserCacho(cachoCandidato.getUser(), new Cacho(cacho.start, cachoLenght));
 	}
 
 	/**
 	 * @param cacho
 	 * @param tempNoPlanRequesterCachos
 	 * @return lista de noRequesterCachos que contienen el cacho(arg0) ordenada de menor a mayor por tamaño de cacho 
 	 */
 	private List<UserCacho> cachosCandidatos(Cacho cacho,
 			List<UserCachos> noPlanRequesterCachos) {
 
 		List<UserCacho> result = new ArrayList<UserCacho>();
 
 		for(UserCachos userCachos : noPlanRequesterCachos){
 			for(Cacho noRequesterCacho : userCachos.cachos){
 				if(noRequesterCacho.contains(cacho)){
 					result.add(new UserCacho(userCachos.user, noRequesterCacho));
 				}
 			}
 		}
 		Collections.sort(result, new CachoSizeComparator());
 		return result;
 	}
 
 }
