 import java.util.Iterator;
 import java.util.Vector;
 
 
 public class Verificateur {
 
 	
 	public boolean verifies(Recommendation reco) throws ExceptionRecoNotValid {
 		//TODO : parler avec l'interpr�te pour rŽcupŽrer la liste d'amis de l'utilisateur faisant la requ�te
 		
 		return true;
 		//Si la vŽrif marche pas :
 		//throw new ExceptionRecoNotValid(ExceptionRecoNotValid.ERR_VERIFICATEUR);
 	}
 	
 	public Vector<Recommendation> verifies(Vector<Recommendation> recos) //throws ExceptionRecoNotValid
 	{
 		Iterator<Recommendation> it = recos.iterator();
 		Recommendation reco;
 		while (it.hasNext())
 		{
 			reco = it.next();
 			try {
 				if (!verifies(reco)) it.remove();
 			}catch (ExceptionRecoNotValid e) {
 				it.remove();
 			}
 		}
 		return recos;
 	}
 	
 }
