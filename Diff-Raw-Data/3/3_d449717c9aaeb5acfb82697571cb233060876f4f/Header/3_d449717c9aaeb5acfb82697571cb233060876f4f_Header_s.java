 package ee.itcollege.robo.client;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Anchor;
 
 import ee.itcollege.robo.client.regform.RegForm;
 import ee.itcollege.robo.client.util.AppConstants;
import ee.itcollege.robo.tournament.Tournament;
 
 public class Header implements ClickHandler{
 
 	private Anchor regForm = new Anchor( AppConstants.CONSTANTS.regFormTab() );
 	private Anchor tournament = new Anchor( AppConstants.CONSTANTS.tournamentTab() );
 	private Anchor match = new Anchor( AppConstants.CONSTANTS.matchTab() );
 	private Anchor admin = new Anchor( AppConstants.CONSTANTS.adminTab() );
 	
 	public Header()
 	{
 		regForm.addClickHandler(this);
 		tournament.addClickHandler(this);
 		match.addClickHandler(this);
 		admin.addClickHandler(this);
 
         ViewHandler.addHeaderLinks( regForm );
         ViewHandler.addHeaderLinks ( tournament );
         ViewHandler.addHeaderLinks( match );
         ViewHandler.addHeaderLinks( admin );
 	}
 
     public void onClick( ClickEvent event )
     {
 		if ( event.getSource() == regForm )
         {
 			// TODO: add history support
             Main.setContent( new RegForm() );
         }
 		else if ( event.getSource() == tournament )
 		{
 			Main.setContent( new Tournament() );
 		}
     }
 	
 	public Anchor getRegForm() {
 		return regForm;
 	}
 
 	public void setRegForm(Anchor regForm) {
 		this.regForm = regForm;
 	}
 
 	public Anchor getTournament() {
 		return tournament;
 	}
 
 	public void setTournament(Anchor tournament) {
 		this.tournament = tournament;
 	}
 
 	public Anchor getMatch() {
 		return match;
 	}
 
 	public void setMatch(Anchor match) {
 		this.match = match;
 	}
 
 	public Anchor getAdmin() {
 		return admin;
 	}
 
 	public void setAdmin(Anchor admin) {
 		this.admin = admin;
 	}
 }
