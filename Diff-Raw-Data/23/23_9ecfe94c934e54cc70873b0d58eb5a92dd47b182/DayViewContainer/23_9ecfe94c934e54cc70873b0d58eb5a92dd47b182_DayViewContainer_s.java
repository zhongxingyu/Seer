 package tim.view.calendar.test;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 
 import javax.swing.JPanel;
 
 import tim.application.GlobalRegistry;
 import tim.application.Resizer;
 import tim.model.Element;
 import tim.view.ChildView;
 import tim.view.ParentView;
 
 public class DayViewContainer extends JPanel implements ChildView {
 	
 	private ArrayList<Element> elements;
 	private UserCalendar userCalendar;
 	private ParentView parentView;
 	private Dimension dimension;
 	private ArrayList<UserCalendar> userCalendars;
 	
 	public DayViewContainer() {
 		GlobalRegistry.resizer.addObserver(this);
 		setOpaque(false);
 		userCalendars = new ArrayList<UserCalendar>();
 	}
 	
 	public void load() {
 		setLayout(new GridLayout(elements.size(),1));
 		
 		for (Element element : elements) {
 			userCalendar = new UserCalendar();
 			userCalendar.setData(element);
 			userCalendar.setParentView(parentView);
 			userCalendar.load();
 			userCalendars.add(userCalendar);
 			add(userCalendar);
 		}
 
 	}
 	
 	
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if (o instanceof Resizer) {
 			this.dimension = (Dimension) arg;
 			setBounds(200,20, (int)dimension.getWidth()-200, (int)dimension.getHeight()-70-20);
			System.out.println((int)dimension.getHeight()-54-20);
 			repaint();
 		}
 
 	}
 	
 	public void goTo (Date date) {
 		for (UserCalendar userCalendar : userCalendars) {
 			userCalendar.reload(date);
 		}
 	}
 
 	@Override
 	public void setParentView(ParentView view) {
 		this.parentView = view;
 
 	}
 
 	@Override
 	public Object getData() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setData(Object value) {
 		this.elements = (ArrayList<Element>) value;
 
 	}
 	
 	public void validate(){
 		dimension = getSize();
 	    setSize(dimension);
 	    setPreferredSize(dimension);
 	}
 	public void paintComponent(Graphics g) {
 	    super.paintComponent(g);
 	    validate();
 	}
 }
