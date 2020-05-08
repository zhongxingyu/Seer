 package xeon.cm.gui.grid.register;
 
 import xeon.cm.dao.CompanyDAO;
 import xeon.cm.dao.ComponentDAO;
 import xeon.cm.dao.ComponentInDAO;
 import xeon.cm.model.ComponentIn;
 
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Vector;
 
 /**
  * User: xeon
  * Date: 3/16/13
  * Time: 8:54 AM
  */
 public class InRegisterFrame extends JFrame implements Register {
 
	private static final long serialVersionUID = -5354471350340778635L;
	
	private static InRegisterFrame instance;
     private JComboBox<String> comps;
     private JFormattedTextField date;
     private JTextField count;
     private JComboBox<String> companies;
     private JTextField price;
     private JTextField actionId;
     private JTextField remark;
 
     private ComponentInDAO componentInDAO;
     private ComponentDAO componentDAO;
     private CompanyDAO companyDAO;
 
     private InRegisterFrame() {
         super("In Register");
         setSize(400, 300);
         build();
         setLocationRelativeTo(null);
 
         componentDAO = new ComponentDAO();
         componentInDAO = new ComponentInDAO();
         companyDAO = new CompanyDAO();
     }
 
     public static InRegisterFrame getInstance() {
         if (instance == null) instance = new InRegisterFrame();
         return instance;
     }
 
     private void build() {
         setLayout(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.insets = new Insets(0, 5, 5, 5);
         constraints.gridwidth = 1;
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         add(new JLabel("Comp: "), constraints);
         String[] compOptions = {"T2234C3", "Component2", "Component3"};
         comps = new JComboBox<>(compOptions);
         constraints.gridx = 1;
         constraints.gridy = 0;
         add(comps, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 1;
         add(new JLabel("Date: "), constraints);
         date = new JFormattedTextField(new SimpleDateFormat("MM/dd/yyyy"));
         date.setColumns(10);
         constraints.gridx = 1;
         constraints.gridy = 1;
         add(date, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 2;
         add(new JLabel("Count: "), constraints);
         count = new JTextField();
         count.setColumns(10);
         constraints.gridx = 1;
         constraints.gridy = 2;
         add(count, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 3;
         add(new JLabel("Company: "), constraints);
 //        String[] companyOptions = {"Company1", "Company2", "Company3"};
         Vector<String> companyOptions = new Vector<>();
         companyOptions.add(0, "Company1");
         companyOptions.add(1, "Company2");
         companyOptions.add(2, "Company3");
         companies = new JComboBox<>(companyOptions);
         constraints.gridx = 1;
         constraints.gridy = 3;
         add(companies, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 4;
         add(new JLabel("Price: "), constraints);
         price = new JTextField();
         price.setColumns(10);
         constraints.gridx = 1;
         constraints.gridy = 4;
         add(price, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 5;
         add(new JLabel("ActionID: "), constraints);
         actionId = new JTextField();
         actionId.setColumns(10);
         constraints.gridx = 1;
         constraints.gridy = 5;
         add(actionId, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 6;
         add(new JLabel("Remark: "), constraints);
         remark = new JTextField();
         remark.setColumns(25);
         constraints.gridx = 1;
         constraints.gridy = 6;
         constraints.gridwidth = 2;
         add(remark, constraints);
 
         add(new RegisterButton(this));
     }
 
     @Override
     public void register() {
         ComponentIn in = new ComponentIn();
 
         in.setComponent(componentDAO.getById(comps.getSelectedItem().toString()));
        @SuppressWarnings("deprecation")
		Date dateValue = new Date(date.getValue().toString());
		in.setDate(dateValue);
         in.setCount(Integer.parseInt(count.getText()));
         in.setCompany(companyDAO.getByName((String) companies.getSelectedItem()));
         in.setPrice(Integer.parseInt(price.getText()));
         in.setActionId(actionId.getText());
         in.setMark(remark.getText());
 
         componentInDAO.save(in);
 
         this.setVisible(false);
         this.validate();
     }
 
 }
