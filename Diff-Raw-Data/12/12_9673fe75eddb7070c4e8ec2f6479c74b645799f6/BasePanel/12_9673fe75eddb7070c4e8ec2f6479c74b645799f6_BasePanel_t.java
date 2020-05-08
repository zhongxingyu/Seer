 package at.ac.tuwien.swag.webapp.in.base;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import at.ac.tuwien.swag.model.dao.BuildingDAO;
 import at.ac.tuwien.swag.model.dao.SquareDAO;
 import at.ac.tuwien.swag.model.domain.Building;
 import at.ac.tuwien.swag.model.domain.BuildingType;
 import at.ac.tuwien.swag.model.domain.Square;
 
 import com.google.inject.Inject;
 
 public abstract class BasePanel extends Panel {
 private static final long serialVersionUID = -7684943204211861124L;
 
 private HashMap<BuildingType, Building> buildings;
 
     @Inject
     private BuildingDAO buildingsDao;
 
     @Inject
     private SquareDAO squareDAO;
 
     private Square square;
 
 	private long squareId;
 
 	private Form<?> buildWood;
 	private Form<?> buildClay;
 	private Form<?> buildStable;
 	private Form<?> buildBarracks;
 	private Form<?> buildUpgrades;
 	private Form<?> buildGrain;
 	private Form<?> buildDestruction;
 	private Form<?> buildIron;
 
 	private BookmarkablePageLink<?> barracksLink;
 	private BookmarkablePageLink<?> stableLink;
 	private BookmarkablePageLink<?> destructionLink;
 	private BookmarkablePageLink<?> upgradeLink;
 	private BookmarkablePageLink<?> troopsLink;

	private FeedbackPanel feedbackPanel;
     
     public BasePanel(String id, long squareId) {
         super(id);
         
         this.squareId = squareId;
         // Retrieves the square
         square = squareDAO.findById(squareId);
 
         fetchBuildings();
 
        feedbackPanel = new FeedbackPanel("feedback");
         add(feedbackPanel);
 
         PageParameters params = new PageParameters();
         params.add("square", squareId);
 
         setupLinks(params);
         setupForm();
         
         checkVisiblityOfLinks();
     }
 
     private void setupLinks(PageParameters params) {
     	 barracksLink		= new BookmarkablePageLink<Object>("barracksLink", Barracks.class, params);
          stableLink			= new BookmarkablePageLink<Object>("stableLink", Stable.class, params);
          destructionLink	= new BookmarkablePageLink<Object>("destructionLink", Destruction.class, params);   
          upgradeLink		= new BookmarkablePageLink<Object>("upgradeLink", Upgrades.class, params);
          troopsLink			= new BookmarkablePageLink<Object>("troopsLink", Troops.class, params);
          
          barracksLink.setVisible(false);
          stableLink.setVisible(false);
          destructionLink.setVisible(false);
          upgradeLink.setVisible(false);
          
          add(barracksLink);
          add(stableLink);
          add(destructionLink);
          add(upgradeLink);
          add(troopsLink );
     }
     
     private void checkVisiblityOfLinks() {
     	 if (buildings.containsKey(BuildingType.BARRACKS)) {
              barracksLink.setVisible(true);
          }
     	 if (buildings.containsKey(BuildingType.STABLE)) {
              stableLink.setVisible(true);
          }
     	 if (buildings.containsKey(BuildingType.DESTRUCTION)) {
     		 destructionLink.setVisible(true);
          }
     	 if (buildings.containsKey(BuildingType.UPGRADE)) {
              upgradeLink.setVisible(true);
          }
     }
     
     private void setupForm() {
         buildWood			= createForm("buildWood", "woodButton", BuildingType.WOOD);
         buildClay			= createForm("buildClay", "clayButton", BuildingType.CLAY);
         buildStable			= createForm("buildStable", "stableButton", BuildingType.STABLE);
         buildBarracks		= createForm("buildBarracks", "barracksButton", BuildingType.BARRACKS);
         buildUpgrades		= createForm("buildUpgrades", "upgradesButton", BuildingType.UPGRADE);
         buildDestruction	= createForm("buildDestruction", "destructionButton", BuildingType.DESTRUCTION);
         buildGrain			= createForm("buildGrain", "grainButton", BuildingType.GRAIN);
         buildIron			= createForm("buildIron", "ironButton", BuildingType.IRON);
 
         add(buildWood);
         add(buildClay);
         add(buildStable);
         add(buildBarracks);
         add(buildUpgrades);
         add(buildDestruction);
         add(buildGrain);
         add(buildIron);
     }
 
     private Form<?> createForm(String form, final String button, final BuildingType type) {
         Form<?> newForm = new Form<Object>(form);
 
         final Building building = buildings.get(type);
 
         AjaxButton newButton = new AjaxButton(button) {
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				//fetchBuildings();
 				Building building = buildings.get(type);
 				
 	                if (building != null) {
 
 	                    Integer newLevel = building.getLevel() + 1;
 	                    building.setLevel(newLevel);
 
 	                    buildingsDao.beginTransaction();
 	                    buildingsDao.update(building);
 	                    buildingsDao.commitTransaction();
 	                    updateBildingCounter(building, this) ;
 
 	                    info("upgraded");
 	                } else {
 
 	                    building = new Building();
 	                    building.setLevel(1);
 	                    building.setType(type);
 	                    building.setUpgrading(false); // TODO --> sanitize
 	                    building.setSquare(square);
 
 	                    buildingsDao.beginTransaction();
 	                    buildingsDao.insert(building);
 	                    buildingsDao.commitTransaction();
 	                    
 	                    updateBildingCounter(building, this) ;
 	                    
 	                    buildings.put(type, building);
	                    info("created");
 	                }
 	                 
 	                target.add(buildWood);
 	                target.add(buildClay);
 	                target.add(buildStable);
 	                target.add(buildBarracks);
 	                target.add(buildUpgrades);
 	                target.add(buildDestruction);
 	                target.add(buildGrain);
 	                target.add(buildIron);
 	                
 	                checkVisiblityOfLinks();
 	                
 	                add(barracksLink);
 	                add(stableLink);
 	                add(destructionLink);
 	                add(upgradeLink);
 	                add(troopsLink );
	                
	                add(feedbackPanel);
 			}
 
 			@Override
 			protected void onError(AjaxRequestTarget target, Form<?> form) {
 				// TODO Auto-generated method stub
 				
 			}
         };
 
         updateBildingCounter(building, newButton);
         newForm.add(newButton);
         return newForm;
     }
 
   
     private void updateBildingCounter(Building building, AjaxButton newButton) {
         	
         	if (building != null) {
 
                 if (building.getLevel() > 9) {
                     newButton.setModel(new Model<String>("maxed"));
                     newButton.setEnabled(false);
                 } else {
                     Integer newLevel = building.getLevel() + 1;
                     newButton.setModel(new Model<String>("upgrade to " + newLevel));
                 }
 
                 if (building.getUpgrading()) {
                     newButton.setEnabled(false);
                 }
 
             } else {
                 newButton.setModel(new Model<String>("build"));
 
                 // TODO check ressources for upgrading
             }
     }
     
     private void fetchBuildings() {
         buildings = new HashMap<BuildingType, Building>();
 
         String query = "SELECT b FROM Building b WHERE b.square.id = :square";
 
         Map<String, Long> values = new HashMap<String, Long>();
         values.put("square", square.getId());
 
         for (Building building : buildingsDao.findByQuery(query, values)) {
             buildings.put(building.getType(), building);
         }
     }
     
    public abstract void onCancel(AjaxRequestTarget target);
    public abstract void onSubmitButton(AjaxRequestTarget target, long squareId);
 
    public abstract void onSelect(AjaxRequestTarget target, String selection);
 }
