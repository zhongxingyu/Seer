 package view;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 /**
  * Tab to display results of searching the index
  */
 public class ResultTab extends JScrollPane {
 
 	private static final long serialVersionUID = -7415080002662301890L;
 
 	private String queryString;
 	private JPanel container;
 
 	public ResultTab(String queryString) {
 		this.queryString = queryString;
 		container = new JPanel();
 	    container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
 
 		setViewportView(container);
 	    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	}
 
 	public void appendResultView(ResultView resultView) {
 		container.add(resultView);
 	}
 
 	public String getQueryString() {
 		return queryString;
 	}
 
 }
