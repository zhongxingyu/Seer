 package views;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.dnd.DropTarget;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 import observer.Observer;
 import views.buttons.AddEntityButton;
 import views.listeners.MenuEntitiesPanelDropTargetListener;
 import controllers.EntityController;
 import controllers.OperationController;
 
 
 public class MenuEntitiesPanel extends JPanel implements Observer
 {	
 	private static final long serialVersionUID = 1L;
 	
	private GlobalPanel _globalPanel;
 	private OperationController _operation;
 	
 	private JPanel _panelAvailable;
 	private JPanel _panelNotAvailable;
 
 	private List<EntityController> _availableEntities;
 	private List<EntityController> _unavailableEntities;
 
	public MenuEntitiesPanel(OperationController operation, GlobalPanel globalPanel)
 	{
 		super();
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		_operation = operation;
		_globalPanel = globalPanel;
 
 		_availableEntities = new ArrayList<EntityController>();
 		_unavailableEntities = new ArrayList<EntityController>();
 		List<EntityController> listEntities = _operation.getEntityList();
 		for(EntityController oneEntity : listEntities) {
 			if(oneEntity.isAvailable()) {
 				_availableEntities.add(oneEntity);
 			} else {
 				_unavailableEntities.add(oneEntity);				
 			}
 		}
 		
		for(EntityController oneEntity : _availableEntities) {
			EntityPanel panel = new EntityPanel(this, oneEntity);
			_panelAvailable.add(panel);
		}
		
		for(EntityController oneEntity : _unavailableEntities) {
			EntityPanel panel = new EntityPanel(this, oneEntity);
			_panelNotAvailable.add(panel);
		}
		
		
 		_panelAvailable = new JPanel();
 		_panelAvailable.setLayout(new BoxLayout(_panelAvailable, BoxLayout.Y_AXIS));
 		_panelAvailable.setAlignmentX(Component.LEFT_ALIGNMENT);
 		_panelAvailable.setBorder(new TitledBorder(new LineBorder(new Color(255, 255, 255), 2), "Disponible", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 255, 255)));
 		_panelNotAvailable = new JPanel();
 		_panelNotAvailable.setAlignmentX(Component.LEFT_ALIGNMENT);
 		_panelNotAvailable.setMinimumSize(new Dimension(MenuPanel.LEFT_PANEL_WIDTH, 0));
 		_panelNotAvailable.setMaximumSize(new Dimension(MenuPanel.LEFT_PANEL_WIDTH, Short.MAX_VALUE));
 		_panelNotAvailable.setBorder(new TitledBorder(new LineBorder(new Color(255, 255, 255), 2), "Indisponible", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 255, 255)));
 		
 		_panelAvailable.setBackground(Color.BLACK);
 		_panelNotAvailable.setBackground(Color.BLACK);
 		
 		add(_panelAvailable, BorderLayout.CENTER);
 		add(_panelNotAvailable, BorderLayout.SOUTH);
 		
 		setOpaque(false);
 		
 		final int WIDTH_PANEL_ADD_BUTTON = MenuPanel.LEFT_PANEL_WIDTH - 13;
 		final int HEIGHT_BUTTON = 25;
 		JPanel panelAddButton = new JPanel();
 		panelAddButton.setLayout(new BorderLayout());
 		panelAddButton.setBackground(Color.BLACK);
 		panelAddButton.setBorder(new EmptyBorder(0, 10, 2, 10));
 		panelAddButton.setMinimumSize(new Dimension(WIDTH_PANEL_ADD_BUTTON, HEIGHT_BUTTON));
 		panelAddButton.setMaximumSize(new Dimension(WIDTH_PANEL_ADD_BUTTON, HEIGHT_BUTTON));
		AddEntityButton addButton = new AddEntityButton(_globalPanel.getMapPanel(), "+");
 		panelAddButton.add(addButton);
 		_panelAvailable.add(panelAddButton);
 	}
 	
 	
 	public void setListEntitiesContent()
 	{
 		List<EntityController> listEntities = _operation.getEntityList();
 		List<EntityController> availableEntities = new ArrayList<EntityController>();
 		List<EntityController> unavailableEntities = new ArrayList<EntityController>();
 		for(EntityController oneEntity : listEntities) {
 			if(oneEntity.isAvailable()) {
 				availableEntities.add(oneEntity);
 			} else {
 				unavailableEntities.add(oneEntity);				
 			}
 		}
 		_availableEntities.retainAll(availableEntities);
 		availableEntities.removeAll(_availableEntities);
 		for(EntityController oneEntity : availableEntities) {
 			EntityPanel panel = new EntityPanel(this, oneEntity);
 			_panelAvailable.add(panel);
 		}
 		_availableEntities.addAll(availableEntities);
 		
 		_unavailableEntities.retainAll(unavailableEntities);
 		unavailableEntities.removeAll(_unavailableEntities);
 		for(EntityController oneEntity : unavailableEntities) {
 			EntityPanel panel = new EntityPanel(this, oneEntity);
 			_panelNotAvailable.add(panel);
 		}
 		_unavailableEntities.addAll(unavailableEntities);
 	}
 	
 	
 	
 	public void addDropTarget() {
		this.setDropTarget(new DropTarget(this, new MenuEntitiesPanelDropTargetListener(_globalPanel)));
 	}
 	
	public GlobalPanel getGlobalPanel() {return _globalPanel;}
 	
 	public OperationController getOperationController()
 	{
 		return _operation;
 	}
 
 	public void enableListeners(boolean enable) {
 //		Component[] com = _panelAvailable.getComponents();
 //		for (int i = 0; i < com.length; i++)
 //		     com[i].enableListener(enable);
 	}
 
 	@Override
 	public synchronized void update()
 	{
 		setListEntitiesContent();	
 	}
 }
