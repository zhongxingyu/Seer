 package sma.gui;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 import javax.swing.border.EtchedBorder;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Component;
 
 //Control used to create a panel info for a certain agent
 public class GraphicAgentPanelInfo extends JPanel {
 	
 	//Variables that indicate the max quantity of seafood per deposit
 	private  Double _maxQty = 0d;
 	//Variables that indicate the current quantity in the deposit
 	private  Double _qtyTuna = 0d, _qtyOctopus = 0d, _qtyLobster = 0d, _qtyShrimp = 0d;
 	private double _money = 0;
 	private String _name = "agent";
 	private String _portType;
 	private boolean _isPort;
 	private Color _color;
 	
 	
 	public void setName(String Name)
 	{
 		_name = Name;
 	}
 	
 	
 	public void setMaxQuantityOfSeafood(Double maxQty)
 	{
 		_maxQty = maxQty;
 	}
 	
 	public void setCurrentQuantityOfSeafood(Double tuna, Double octopus, Double shrimp, Double lobster)
 	{
 		if (tuna != null)
 			_qtyTuna = tuna;
 		if (octopus != null)
 			_qtyOctopus = octopus;
 		if (shrimp != null)
 			_qtyShrimp = shrimp;
 		if (lobster != null)
 			_qtyLobster = lobster;
 	}
 	
 	public Double getCurrentQuantityOfTuna()
 	{
 		return _qtyTuna;
 	}
 	
 	public Double getCurrentQuantityOfOctopus()
 	{
 		return _qtyOctopus;
 	}
 	
 	public Double getCurrentQuantityOfShrimp()
 	{
 		return _qtyShrimp;
 	}
 	
 	public Double getCurrentQuantityOfLobster()
 	{
 		return _qtyLobster;
 	}
 	
 	public void setMoneyAvailable(double Money)
 	{
 		_money = Money;
 	}
 	
 	public void setPortType(String portType)
 	{
 		_portType = portType;
 	}
 	
 	
 	public Double getMaxQuantityOfSeafoodPerSeafood()
 	{
 		return _maxQty ;
 	}
 
 	
 	public double getAvailableMoney()
 	{
 		return _money;
 	}
 	
 	public String getPortType()
 	{
 		return _portType;
 	}
 	
 	public String getName()
 	{
 		return _name;
 	}
 	
 	JLabel lblName, lblAvMoney, lblPortType;
 	JLabel lblTunaStatus;
 	JLabel lblOctopusStatus;
 	JLabel lblLobsterStatus;
 	JLabel lblShrimpStatus;
 	
 	public GraphicAgentPanelInfo(AgentPanelType Type)
 	{
 		
 		setPreferredSize(new Dimension(96, 68));
 		setSize(new Dimension(99, 75));
 		
 		setMinimumSize(new Dimension(99, 75));
 		setLayout(null);
 		
 		lblName = new JLabel("Agent Name");
 		lblName.setFont(new Font("Tahoma", Font.BOLD, 10));
 		lblName.setBounds(2, 3, 88, 14);
 		add(lblName);
 		
 		lblAvMoney = new JLabel("Money");
 		lblAvMoney.setFont(new Font("Tahoma", Font.ITALIC, 9));
 		lblAvMoney.setBounds(65, 20, 95, 14);
 		add(lblAvMoney);
 		
 		lblTunaStatus = new JLabel("T");
 		lblTunaStatus.setFont(new Font("Tahoma", Font.PLAIN, 8));
 		lblTunaStatus.setBounds(4, 11, 64, 23);
 		lblTunaStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
 		add(lblTunaStatus);
 		
 		lblOctopusStatus = new JLabel("O");
 		lblOctopusStatus.setFont(new Font("Tahoma", Font.PLAIN, 8));
 		lblOctopusStatus.setBounds(3, 27, 64, 14);
 		add(lblOctopusStatus);
 		
 		lblLobsterStatus = new JLabel("L");
 		lblLobsterStatus.setFont(new Font("Tahoma", Font.PLAIN, 8));
 		lblLobsterStatus.setBounds(5, 53, 64, 13);
 		add(lblLobsterStatus);
 		
 		lblShrimpStatus = new JLabel("S");
 		lblShrimpStatus.setFont(new Font("Tahoma", Font.PLAIN, 8));
 		lblShrimpStatus.setBounds(4, 41, 64, 13);
 		add(lblShrimpStatus);
 		
 		lblPortType = new JLabel("Port");
 		lblPortType.setFont(new Font("Tahoma", Font.ITALIC, 9));
 		lblPortType.setBounds(65, 3, 46, 14);
 		add(lblPortType);
 		
 		if (Type == AgentPanelType.Port)
 		{
 			_isPort = true;
 			_color = Color.RED;
 			lblAvMoney.setVisible(true);
 			lblPortType.setVisible(true);
 		}
 		else
 		{
 			_isPort = false;
 			_color = Color.BLUE;
 			lblAvMoney.setVisible(false);
 			lblPortType.setVisible(false);
 		}
 		setBorder(new EtchedBorder(EtchedBorder.LOWERED, _color, null));
 		
 		
 		
 		setInfo();
 	}
 	
 	public void setInfo()
 	{
 		lblName.setText(getName());
 		
 		lblTunaStatus.setText("T: " + getCurrentQuantityOfTuna() + "/" + this._maxQty);
 		lblOctopusStatus.setText("O: " + getCurrentQuantityOfOctopus() + "/" + this._maxQty);
 		lblLobsterStatus.setText("L: " + getCurrentQuantityOfLobster() + "/" + this._maxQty);
 		lblShrimpStatus.setText("S: " + getCurrentQuantityOfShrimp() + "/" + this._maxQty);
 		
 		if (_isPort)
 		{
			lblAvMoney.setText(": " + Double.toString(getAvailableMoney()));
 			lblPortType.setText(_portType);
 		}
 	}
 }
