 package plarktmaatsActions.gebruikers;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import plarktmaatsAware.UserAware;
 import plarktmaatsDAO.BodDAOImpl;
 import plarktmaatsDAO.VeilingDAOImpl;
 import plarktmaatsDomein.Bod;
 import plarktmaatsDomein.Gebruiker;
 import plarktmaatsDomein.Persoon;
 import plarktmaatsDomein.Veiling;
 import tools.ProjectTools;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 public class VoegBodToe extends ActionSupport implements UserAware {
 	private static final long serialVersionUID = 1L;
 	private String id;
 	private String credits;
 	private Persoon user;
 
 	public String execute() {
 		Gebruiker g = (Gebruiker)user;
 		int bedrag = Integer.parseInt(credits);
 		Bod b = new Bod(0, bedrag, g, id);
 		BodDAOImpl bodDAO = new BodDAOImpl();
 		bodDAO.create(b);
 		
 		return SUCCESS;
 	}
 	
 	public void validate() {
 		if(!ProjectTools.isNumeric(credits)) {
			addFieldError("credits", "Vul een bod in");
 			return;
 		}
 		VeilingDAOImpl veilingDAO = new VeilingDAOImpl();
 		Veiling v = veilingDAO.read(id);
 		if(v == null) {
 			addFieldError("credits", "Deze veiling bestaat niet (meer)");
 			return;
 		}
 		if(v.getGeblokkeerd()) {
 			addFieldError("credits", "Deze veiling is geblokkeerd");
 		}
 		int creditsInt = Integer.parseInt(credits);
 		if(v.getMinBedrag() > creditsInt) {
 			addFieldError("credits", "Dit bod is onder het minimumbedrag");
 		}
 		if(v.getHoogsteBod() != null) {
 			if(v.getHoogsteBod().getBedrag() >= creditsInt) {
 				addFieldError("credits", "Er is al hoger geboden");
 			}
 		}
 		if(!(user instanceof Gebruiker)) {
 			addFieldError("credits", "Alleen gebruikers mogen bieden");
 			return;
 		}
 		Gebruiker usergebruiker = (Gebruiker)user;
 		if(usergebruiker.getGeblokkeerd()) {
 			addFieldError("credits", "U mag niet bieden");
 		}
 		Date eindTijd = v.getEindTijd().getTime();
 		Date nu = Calendar.getInstance().getTime();
 		long diff = (eindTijd.getTime() - nu.getTime())/1000;
 		if(diff < 0) {
 			addFieldError("credits", "De veiling is gesloten");
 		}
 
 	}
 	
 	public String getCredits() {
 		return credits;
 	}
 	public void setCredits(String credits) {
 		this.credits = credits;
 	}
 	
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	@Override
 	public void setUser(Persoon user) {
 		this.user = user;
 	}
 }
