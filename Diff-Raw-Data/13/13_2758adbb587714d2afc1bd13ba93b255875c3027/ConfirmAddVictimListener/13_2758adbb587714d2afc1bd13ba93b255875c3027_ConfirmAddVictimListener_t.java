 package views.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.util.Date;
 
 import javax.swing.SwingUtilities;
 
 import views.AddVictimPanel;
 import views.CustomDialog;
 import views.GlobalPanel;
 import views.MapPanel;
 import views.MessagePanel;
 import controllers.EntityController;
 import controllers.OperationController;
 import controllers.VictimController;
 import database.DatabaseManager;
 
 
 
 public class ConfirmAddVictimListener implements ActionListener
 {
 	public static String EMPTY_MOTIF_MESSAGE = "Veuillez renseigner soit le champ \"Motif\", soit le champ \"Autre motif\".";
 	public static String EMPTY_ID_ANONYMAT_MESSAGE = "Veuillez renseigner le champ \"Id d'anonymat\".";
 	public static String EMPTY_SOINS_MESSAGE = "Veuillez renseigner le champ \"Soins\".";
 	public static String EMPTY_ENTITY_ASSOCIATED_MESSAGE = "Veuillez renseigner le champ \"Entité associée\".";
 	
 	private MapPanel _mapPanel;
 	private GlobalPanel _globalPanel;
 	private OperationController _operationController;
 	private DatabaseManager _databaseManager;
 	private AddVictimPanel _addVictimPanel;
 	
 	
 	public ConfirmAddVictimListener(MapPanel mapPanel, OperationController operationController, DatabaseManager databaseManager, AddVictimPanel addVictimPanel)
 	{
 		_mapPanel = mapPanel;
 		_globalPanel = _mapPanel.getGlobalPanel();
 		_operationController = operationController;
 		_databaseManager = databaseManager;
 		_addVictimPanel = addVictimPanel;
 	}
 	
 	
 	public static boolean checkInput(String motif, String otherMotif, String idAnomymat, EntityController entityAssociated)
 	{
 		return ((!motif.equals("") || (!otherMotif.equals(""))) && (idAnomymat != null) && (entityAssociated != null));
 	}
 	
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		Object[] objects = _addVictimPanel.getMotifList().getSelectedValuesList().toArray();
 		String[] motifsList = new String[objects.length];
 		for(int i = 0; i < objects.length; i++)
 			motifsList[i] = (String)objects[i];
 		String otherMotif = _addVictimPanel.getDetailsTextArea().getText();
 		String idAnonymat = _addVictimPanel.getIdAnonymat().getText();
 		String soins = _addVictimPanel.getSoins().getText();
 		EntityController entitesAssociees = _addVictimPanel.getMap().get((String)_addVictimPanel.getEntiteAssocieeCombobox().getSelectedItem());
 				
 		if(!checkInput(((motifsList.length == 0 ) || (motifsList[0].equals("(Autre motif)"))) ? "" : motifsList[0], otherMotif, idAnonymat, entitesAssociees))
 		{
 			if(((motifsList.length == 0) || (motifsList[0].equals("(Autre motif)"))) && (otherMotif.equals("")))
 			{
 				MessagePanel errorPanel = new MessagePanel("Saisie incomplète", EMPTY_MOTIF_MESSAGE);
 				new CustomDialog(errorPanel, _globalPanel);
 			}
 			else
 				if(idAnonymat.equals(""))
 				{
 					MessagePanel errorPanel = new MessagePanel("Saisie incomplète", EMPTY_ID_ANONYMAT_MESSAGE);
 					new CustomDialog(errorPanel, _globalPanel);
 				}
 				else
 					if(soins.equals(""))
 					{
 						MessagePanel errorPanel = new MessagePanel("Saisie incomplète", EMPTY_SOINS_MESSAGE);
 						new CustomDialog(errorPanel, _globalPanel);
 					}
 					else
 						if(entitesAssociees == null)
 						{
 							MessagePanel errorPanel = new MessagePanel("Saisie incomplète", EMPTY_ENTITY_ASSOCIATED_MESSAGE);
 							new CustomDialog(errorPanel, _globalPanel);
 						}
 		}
 		else
 		{
 			String name = _addVictimPanel.getNameTextField().getText();
 			String prenom = _addVictimPanel.getPrenomTextField().getText();
 			String adress = _addVictimPanel.getAdressTextField().getText();
 			
 			Date dateDeNaissanceDate = _addVictimPanel.getDateDeNaissanceDatePicker().getDate();
 			Timestamp dateDeNaissance = null;
 			if(dateDeNaissanceDate != null)
 				dateDeNaissance = new Timestamp(dateDeNaissanceDate.getYear(),  dateDeNaissanceDate.getMonth(), dateDeNaissanceDate.getDate(), dateDeNaissanceDate.getHours(), dateDeNaissanceDate.getMinutes(), dateDeNaissanceDate.getSeconds(), 0);
 			
 			try
 			{
 				new VictimController(_operationController, _databaseManager, name, prenom, motifsList, adress, dateDeNaissance, otherMotif, soins, idAnonymat, entitesAssociees);
 			}
 			catch(ParseException e1) {e1.printStackTrace();}
 			
 			CustomDialog dialog = (CustomDialog) SwingUtilities.getAncestorOfClass(CustomDialog.class,_addVictimPanel);
 			dialog.dispose();
 		}
 	}
 }
