 package lorian.graph;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import lorian.graph.function.Function;
 import lorian.graph.function.MathChars;
 import lorian.graph.function.PointXY;
 import lorian.graph.function.Util;
 import lorian.graph.function.VisualPoint;
 import lorian.graph.function.VisualPointLocationChangeListener;
 
 public class CalculateFrame extends JPanel implements ActionListener, ChangeListener, VisualPointLocationChangeListener {
 	private static final long serialVersionUID = -6709615022829676720L;
 	private Calculation calc;
 	private String title;
 	private SpringLayout layout;
 	private int height = 5;
 	
 	private JComboBox<String> funcComboBox, funcComboBox2;
 	private JSpinner x1, x2;
 	private JLabel resultLabel;
 	
 	private VisualPoint lowx, upx;
 	
 	private boolean calculated = false;
 	enum Calculation
 	{
 		VALUE, ZERO, MINIMUM, MAXIMUM, INTERSECT, DYDX, INTEGRAL;
 	}
 	
 	private void initGeneralUI()
 	{
 		layout = new SpringLayout();
 		this.setLayout(layout);
 
 		JLabel titlelabel = new JLabel(title, JLabel.CENTER);
 		titlelabel.setFont(titlelabel.getFont().deriveFont(13.0f).deriveFont(Font.BOLD));
 		this.add(titlelabel);
 		
 		SpringLayout.Constraints titleCons = layout.getConstraints(titlelabel);
 		//titleCons.setX(Spring.sum(Spring.constant(40), titleCons.getConstraint(SpringLayout.HORIZONTAL_CENTER)));
 		titleCons.setX(Spring.constant(80));
 		titleCons.setY(Spring.constant(height)); 
 		
 		
 		JButton closeButton = new JButton();
 		closeButton.setPreferredSize(new Dimension(25, 25));
 		closeButton.setName("close");
 		closeButton.addActionListener(this);
 		try
 		{
 			Image img = ImageIO.read(getClass().getResource("/res/close.png"));
 			closeButton.setIcon(new ImageIcon(img));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			closeButton.setText("x");
 		}
 				
 		this.add(closeButton);
 		SpringLayout.Constraints closeButtonCons = layout.getConstraints(closeButton);
 		closeButtonCons.setX(Spring.sum(Spring.constant(220), closeButtonCons.getConstraint(SpringLayout.EAST)));
 		closeButtonCons.setY(Spring.constant(0)); 
 		
 		height += titlelabel.getPreferredSize().getHeight() + 10;
 	}  
 	private void AddCalculateButton()
 	{
 
 		JButton calcButton = new JButton("Calculate");
 		calcButton.addActionListener(this);
 		calcButton.setName("calculate");
 		this.add(calcButton);
 		
 		SpringLayout.Constraints calcButtonCons = layout.getConstraints(calcButton);
 		calcButtonCons.setX(Spring.constant(80));
 		calcButtonCons.setY(Spring.constant(height));
 		if(this.calc != Calculation.INTERSECT)
 			height += calcButton.getPreferredSize().getHeight() + 15;
 		else
 			height += calcButton.getPreferredSize().getHeight() + 3; 
 		
 		resultLabel = new JLabel("Result");
 		resultLabel.setFont(resultLabel.getFont().deriveFont(13.0f));
 		resultLabel.setVisible(false);
 		GraphFunctionsFrame.gframe.SetVisualPointsVisible(false);
 		this.add(resultLabel);
 		
 		SpringLayout.Constraints resultCons = layout.getConstraints(resultLabel);
 		resultCons.setX(Spring.constant(80));
 		resultCons.setY(Spring.constant(height));
 	}
 	private void initMovablePoints(double lowxval, double upxval)
 	{
 		if(funcComboBox.getItemCount() == 0) return;
 		int funcindex;
 		try
 		{
 			funcindex = Integer.parseInt(((String) funcComboBox.getSelectedItem()).substring(1)) - 1;
 		}
 		catch (Exception e)
 		{
 			System.out.println("Error parsing function index");
 			return;
 		}
 		
 		Function f = GraphFunctionsFrame.functions.get(funcindex);
 		
 		lowx = new VisualPoint(new PointXY(lowxval, f.Calc(lowxval)), funcindex, true, false, "Lower limit");
 		upx = new VisualPoint(new PointXY(upxval, f.Calc(upxval)), funcindex, true, false, "Upper limit");
 		lowx.addLocationChangedListener(this);
 		upx.addLocationChangedListener(this);
 		GraphFunctionsFrame.gframe.ClearVisualPoints();
 		GraphFunctionsFrame.gframe.AddVisualPoint(lowx);
 		GraphFunctionsFrame.gframe.AddVisualPoint(upx);
 		GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 	}
 	private JComboBox<String> initFunctionCombobox(String comboboxName, String labelText, int x)
 	{	
 		JComboBox<String> ComboBox = new JComboBox<String>(GetActiveFunctions());
 		JLabel functionLabel = new JLabel(labelText);
 		functionLabel.setFont(functionLabel.getFont().deriveFont(13.0f));
 		ComboBox = new JComboBox<String>(GetActiveFunctions());
 		ComboBox.setName(comboboxName);
 		ComboBox.addActionListener(this);
 			
 
 		this.add(functionLabel);
 		this.add(ComboBox);
 		
 		SpringLayout.Constraints labelCons = layout.getConstraints(functionLabel);
 		labelCons.setX(Spring.constant(60));
 		labelCons.setY(Spring.constant(height));
 			
 		SpringLayout.Constraints comboboxCons = layout.getConstraints(ComboBox);
 		if(x == -1)
 			comboboxCons.setX(Spring.sum(labelCons.getConstraint(SpringLayout.EAST), Spring.constant(10)));
 		else
 			comboboxCons.setX(Spring.constant(x));
 		
 		comboboxCons.setY(Spring.constant(height));
 				
 		height += functionLabel.getPreferredSize().getHeight() + 10;
 		return  ComboBox;
 	}
 	private void initLowUpX(int x)
 	{
 		JLabel xLowLabel = new JLabel("Lower limit:");
 		xLowLabel.setFont(xLowLabel.getFont().deriveFont(13.0f));
 		SpinnerNumberModel sModel = new SpinnerNumberModel(-2.0,  Long.MIN_VALUE, Long.MAX_VALUE, 1.0); 
 		x1 = new JSpinner(sModel);
 		x1.addChangeListener(this);
 		x1.setName("lowX");
 		x1.setPreferredSize(new Dimension(80, (int) x1.getPreferredSize().getHeight()));
 
 		this.add(xLowLabel);
 		this.add(x1);
 		
 		SpringLayout.Constraints labelCons = layout.getConstraints(xLowLabel);
 		labelCons.setX(Spring.constant(60));
 		labelCons.setY(Spring.constant(height));
 		SpringLayout.Constraints spinnerCons = layout.getConstraints(x1);
 		if(x == -1)
 			spinnerCons.setX(Spring.sum(labelCons.getConstraint(SpringLayout.EAST), Spring.constant(10)));
 		else 
 			spinnerCons.setX(Spring.constant(x));
 		spinnerCons.setY(Spring.constant(height));
 		
 		height += xLowLabel.getPreferredSize().getHeight() + 10;
 		
 		
 		
 		JLabel xUpLabel = new JLabel("Upper limit:");
 		xUpLabel.setFont(xUpLabel.getFont().deriveFont(13.0f));
 		sModel = new SpinnerNumberModel(2.0,  Long.MIN_VALUE, Long.MAX_VALUE, 1.0); 
 		x2 = new JSpinner(sModel);
 		x2.addChangeListener(this);
 		x2.setName("upX");
 		x2.setPreferredSize(new Dimension(80, (int) x1.getPreferredSize().getHeight()));
 
 		this.add(xUpLabel);
 		this.add(x2);
 		
 		labelCons = layout.getConstraints(xUpLabel);
 		labelCons.setX(Spring.constant(60));
 		labelCons.setY(Spring.constant(height));
 		spinnerCons = layout.getConstraints(x2);
 		if(x == -1)
 			spinnerCons.setX(Spring.sum(labelCons.getConstraint(SpringLayout.EAST), Spring.constant(10)));
 		else 
 			spinnerCons.setX(Spring.constant(x));
 		spinnerCons.setY(Spring.constant(height));
 		
 		height += xUpLabel.getPreferredSize().getHeight() + 15;
 	}
 	
 	
 	
 	
 	private void initValueOrDyDxUI()
 	{
 		initGeneralUI();
 		funcComboBox = initFunctionCombobox("function1", "Function: ", -1); 
 		// X
 		JLabel xLabel = new JLabel("X:");
 		xLabel.setFont(xLabel.getFont().deriveFont(13.0f));
 		SpinnerNumberModel sModel = new SpinnerNumberModel(1.0,  Long.MIN_VALUE, Long.MAX_VALUE, 1.0); 
 		x1 = new JSpinner(sModel);
 		x1.setPreferredSize(new Dimension(80, (int) x1.getPreferredSize().getHeight()));
 
 		this.add(xLabel);
 		this.add(x1);
 		
 		SpringLayout.Constraints labelCons = layout.getConstraints(xLabel);
 		labelCons.setX(Spring.constant(60));
 		labelCons.setY(Spring.constant(height));
 		SpringLayout.Constraints spinnerCons = layout.getConstraints(x1);
 		spinnerCons.setX(Spring.constant(126));
 		spinnerCons.setY(Spring.constant(height));
 		
 		height += xLabel.getPreferredSize().getHeight() + 15;
 		
 		// Calculate button	
 		AddCalculateButton();
 		
 	}
 	
 	private void initLowUpXUI()
 	{
 		initGeneralUI();
 		funcComboBox = initFunctionCombobox("function1", "Function: ", 135); 
 		initLowUpX(135);
 		AddCalculateButton();
 		initMovablePoints(-2, 2);
 	}
 	private void initZeroUI()
 	{
 		initLowUpXUI();
 	}
 	private void initMinOrMaxUI()
 	{
 		initLowUpXUI();
 	}
 	private void initIntersectUI()
 	{
 		initGeneralUI();
 		funcComboBox = initFunctionCombobox("function1", "Function 1:", -1);
 		funcComboBox2 = initFunctionCombobox("function2", "Function 2:", -1);
 		if(funcComboBox2.getItemCount() > 1) funcComboBox2.setSelectedIndex(1);
 		initLowUpX(-1);
 		AddCalculateButton();
 		initMovablePoints(-2, 2);
 		
 	}
 	private void initIntegralUI()
 	{
 		initLowUpXUI();
 		GraphFunctionsFrame.gframe.SetFillFunction(true);
 		int funcindex;
 		if(funcComboBox.getItemCount() > 0)		
 		{
 			try
 			{
 				funcindex = Integer.parseInt(((String) funcComboBox.getSelectedItem()).substring(1)) - 1;
 			}
 			catch (Exception e)
 			{
 				System.out.println("Error parsing function index");
 				return;
 			}
 			GraphFunctionsFrame.gframe.SetFillFunctionIndex(funcindex);
 		}
 	
 		GraphFunctionsFrame.gframe.SetFillLowerLimit(-2);
 		GraphFunctionsFrame.gframe.SetFillUpperLimit(2);
 	}
 	
 	private String[] GetActiveFunctions()
 	{
 		List<String> activefunctions = new ArrayList<String>();
 		for(int i=0;i< GraphFunctionsFrame.functions.size(); i++)
 		{
 			if(GraphFunctionsFrame.functions.get(i).isEmpty() || GraphFunctionsFrame.functions.get(i).drawOn() == false) continue;
 			activefunctions.add("Y" + (i+1));
 		}
 		return activefunctions.toArray(new String[activefunctions.size()]);
 	}
 	public CalculateFrame(Calculation calc)
 	{
 		this.calc = calc;
 		title = "Calculate: ";
 		if(this.calc != Calculation.INTEGRAL)
 			GraphFunctionsFrame.gframe.SetFillFunction(false);
 		
 		switch(this.calc)
 		{
 		case VALUE:
 			title += "Value";
 			initValueOrDyDxUI();
 			break;
 		case ZERO:
 			title += "Zero";
 			initZeroUI();
 			break;
 		case MINIMUM:
 			title += "Minimum";
 			initMinOrMaxUI();
 			break;
 		case MAXIMUM:
 			title += "Maximum";
 			initMinOrMaxUI();
 			break;
 		case INTERSECT:
 			title += "Intersect";
 			initIntersectUI();
 			break;
 		case DYDX:
 			title += "dy/dx";
 			initValueOrDyDxUI();
 			break;
 		case INTEGRAL:
 			title += (MathChars.Integral.getCode() + "f(x)dx");
 			initIntegralUI();
 			break;
 		default:
 			break;
 		}
 		this.setPreferredSize(new Dimension(275, 200));
 		this.setVisible(true);
 	}
 	
 	private void Calculate()
 	{
 		if(this.funcComboBox.getItemCount() == 0 || x1 == null) return;
 		int func1index = Integer.parseInt(((String) funcComboBox.getSelectedItem()).substring(1)) - 1;
 		int func2index = -1;
 		double x1val = (Double) x1.getValue();
 		double x2val = Double.NaN;
 		if(x2 != null)
 			x2val = (Double) x2.getValue();
 		if(funcComboBox2 != null) func2index = Integer.parseInt(((String) funcComboBox2.getSelectedItem()).substring(1)) - 1;
 		
 		String resultstr;
 		switch(this.calc)
 		{
 		case VALUE:
 			double result = GraphFunctionsFrame.functions.get(func1index).Calc(x1val);
 			resultstr = String.format("X = %s, Y = %s", Util.GetString(x1val), Util.GetString(result));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(!Double.isInfinite(result) && !Double.isNaN(result))
 			{
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 				GraphFunctionsFrame.gframe.AddVisualPoint(new VisualPoint(new PointXY(x1val, result), func1index, false, true));
 			}
 			
 			resultLabel.setVisible(true);
 			break;
 		case ZERO:
 			PointXY zeropoint = lorian.graph.function.Calculate.Zero(GraphFunctionsFrame.functions.get(func1index), x1val, x2val);
 			if(Double.isNaN(zeropoint.getX()))
 				resultstr = "Could not calculate zero";
 			else
 				resultstr = String.format("Zero: X = %s, Y = %s", Util.GetString(zeropoint.getX()), Util.GetString(zeropoint.getY()));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(!Double.isInfinite(zeropoint.getY()) && !Double.isNaN(zeropoint.getY()))
 			{
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 				GraphFunctionsFrame.gframe.AddVisualPoint(new VisualPoint(zeropoint, func1index, false, true));
 			}
 			
 			resultLabel.setVisible(true);
 			break;
 		case MINIMUM:
 			PointXY min = lorian.graph.function.Calculate.Minimum(GraphFunctionsFrame.functions.get(func1index), x1val, x2val);
 			resultstr = String.format("Minimum: X = %s, Y = %s", Util.GetString(min.getX()), Util.GetString(min.getY()));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(!Double.isInfinite(min.getY()) && !Double.isNaN(min.getY()))
 			{
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 				GraphFunctionsFrame.gframe.AddVisualPoint(new VisualPoint(min, func1index, false, true));
 			}
 			
 			resultLabel.setVisible(true); 
 			break;
 		case MAXIMUM:
 			PointXY max = lorian.graph.function.Calculate.Maximum(GraphFunctionsFrame.functions.get(func1index), x1val, x2val);
 			resultstr = String.format("Maximum: X = %s, Y = %s", Util.GetString(max .getX()), Util.GetString(max .getY()));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(!Double.isInfinite(max .getY()) && !Double.isNaN(max .getY()))
 			{
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 				GraphFunctionsFrame.gframe.AddVisualPoint(new VisualPoint(max , func1index, false, true));
 			}
 			
 			resultLabel.setVisible(true);
 			break;
 		case INTERSECT:
 			if(func1index == func2index)
 			{
 				resultLabel.setText("Cannot intersect same function!");
 				resultLabel.setVisible(true);
 				return;
 			}
 			PointXY intersectpoint = lorian.graph.function.Calculate.Intersect(GraphFunctionsFrame.functions.get(func1index), GraphFunctionsFrame.functions.get(func2index), x1val, x2val);
 			if(Double.isNaN(intersectpoint.getX()))
 				resultstr = "Could not calculate intersection";
 			else
 				resultstr = String.format("Intersection: X = %s, Y = %s", Util.GetString(intersectpoint .getX()), Util.GetString(intersectpoint .getY()));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(!Double.isInfinite(intersectpoint .getY()) && !Double.isNaN(intersectpoint .getY()))
 			{
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 				GraphFunctionsFrame.gframe.AddVisualPoint(new VisualPoint(intersectpoint , func1index, false, true));
 			}
 			
 			resultLabel.setVisible(true);
 			break;
 		case DYDX:
 			double dydx = lorian.graph.function.Calculate.DyDx(GraphFunctionsFrame.functions.get(func1index), x1val);
 			resultstr = String.format("dy/dx: %s", Util.GetString(dydx));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(!Double.isInfinite(dydx) && !Double.isNaN(dydx))
 			{ 
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(true);
 				GraphFunctionsFrame.gframe.AddVisualPoint(new VisualPoint(new PointXY(x1val, GraphFunctionsFrame.functions.get(func1index).Calc(x1val)), func1index, false, true, resultstr));
 			}
 			
 			resultLabel.setVisible(true);
 			break;
 		case INTEGRAL:
 			double integral = Util.round(lorian.graph.function.Calculate.Integral(GraphFunctionsFrame.functions.get(func1index), x1val, x2val), 6);
 			resultstr = String.format("%cf(x)dx: %s", MathChars.Integral.getCode(), Util.GetString(integral));
 			resultLabel.setText(resultstr);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			GraphFunctionsFrame.gframe.SetVisualPointsVisible(false);
 			resultLabel.setVisible(true);
 		
 			break;
 		default:
 			break;
 		}
 		calculated = true;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() instanceof JButton)
 		{
 			JButton source = (JButton) e.getSource();
 			if(source.getName().equalsIgnoreCase("calculate"))
 			{
 				Calculate();
 			}
 			else if(source.getName().equalsIgnoreCase("close"))
 			{
 				this.setVisible(false);
 				resultLabel.setVisible(false);
 				GraphFunctionsFrame.gframe.setCalcPanelVisible(false);
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(false);
 				GraphFunctionsFrame.gframe.ClearVisualPoints();
 				GraphFunctionsFrame.gframe.SetVisualPointsVisible(false);
 				GraphFunctionsFrame.gframe.SetFillFunction(false);
 			}
 		}
 		
 		else if(e.getSource() instanceof JComboBox<?>)
 		{
 			@SuppressWarnings("unchecked")
 			JComboBox<String> source = (JComboBox<String>) e.getSource();
 			
 			if(source.getName().equalsIgnoreCase("function1"))
 			{
 				resultLabel.setVisible(false);
 				
 				GraphFunctionsFrame.gframe.ClearVisualPoints();
 				if(this.calc != Calculation.VALUE && this.calc != Calculation.DYDX)
 					initMovablePoints((Double) x1.getValue(), (Double)  x2.getValue());
 				
 				if(this.calc == Calculation.INTEGRAL)
 				{
 					if(funcComboBox.getItemCount() == 0) return;
 					
 					int funcindex;
 					try
 					{
 						funcindex = Integer.parseInt(((String) funcComboBox.getSelectedItem()).substring(1)) - 1;
 					}
 					catch (Exception ex)
 					{
 						System.out.println("Error parsing function index");
 						return;
 					}
 					
 					GraphFunctionsFrame.gframe.SetFillFunctionIndex(funcindex);
 				}
 			}
 		}
 		
 		
 	}
 	
 	@Override
 	public void OnLocationChange(VisualPoint p) {
 		if(p.getLabel().equalsIgnoreCase("lower limit"))
 		{
 			if(p.getPoint().getX() >= upx.getPoint().getX())
 			{
 				p.setPoint(new PointXY(upx.getPoint().getX(), p.getPoint().getY()), false); 
 			}
 			else
 			{
 				x1.setValue(p.getPoint().getX());
 			}
 			GraphFunctionsFrame.gframe.SetFillLowerLimit(p.getPoint().getX());
 		}
 		else if(p.getLabel().equalsIgnoreCase("upper limit"))
 		{
 			if(p.getPoint().getX() <= lowx.getPoint().getX())
 			{
 				p.setPoint(new PointXY(lowx.getPoint().getX(), p.getPoint().getY()), false); 
 			}
 			else
 			{
 				x2.setValue(p.getPoint().getX());
 			}
 			GraphFunctionsFrame.gframe.SetFillUpperLimit(p.getPoint().getX());
 		
 			
 		}
 		
 	}
 	
 	public void Update()
 	{
 		
 		if(funcComboBox != null)
 		{
 			funcComboBox.setModel(new JComboBox<String>(GetActiveFunctions()).getModel());
 		}
 		if(funcComboBox2 != null) {
 			funcComboBox2.setModel(new JComboBox<String>(GetActiveFunctions()).getModel());
 			if(funcComboBox2.getItemCount() > 1)
 			{				
 				funcComboBox2.setSelectedIndex(1);
 			}
 			
 			if(funcComboBox.getSelectedIndex() == funcComboBox2.getSelectedIndex() && funcComboBox.getItemCount() > 1)
 				funcComboBox2.setSelectedIndex(funcComboBox2.getSelectedIndex());
 				
 		}
 		resultLabel.setVisible(false);
 		GraphFunctionsFrame.gframe.SetVisualPointsVisible(false);
		if(this.calc != Calculation.VALUE && this.calc != Calculation.DYDX && funcComboBox.getItemCount() > 0 && this.isVisible())
 			initMovablePoints((Double) x1.getValue(), (Double)  x2.getValue());
 
 			
 		
 		if(this.calc == Calculation.INTEGRAL)
 		{
 			if(funcComboBox.getItemCount() > 0)		
 			{
 				int funcindex;
 				try
 				{
 					funcindex = Integer.parseInt(((String) funcComboBox.getSelectedItem()).substring(1)) - 1;
 					GraphFunctionsFrame.gframe.SetFillFunctionIndex(funcindex);
 				}
 				catch (Exception e)
 				{
 					System.out.println("Error parsing function index");
 				}
 			}
 		
 		}
 			
 		
 		
 	}
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		JSpinner source = (JSpinner) e.getSource();
 		if(calculated)
 		{
 			resultLabel.setVisible(false);
 			GraphFunctionsFrame.gframe.ClearVisualPoints();
 			if(this.calc != Calculation.VALUE && this.calc != Calculation.DYDX)
 				initMovablePoints((Double) x1.getValue(), (Double)  x2.getValue());
 			
 		}
 		if(lowx == null || upx == null) return;
 		
 		int funcindex = this.lowx.getFunctionIndex();
 		
 		Function f = GraphFunctionsFrame.functions.get(funcindex);
 		
 		if(source.getName().equalsIgnoreCase("lowX"))
 		{
 			if((Double) source.getValue() > (Double) x2.getValue())
 			{
 				source.setValue(x2.getValue());
 			}
 			else
 				GraphFunctionsFrame.gframe.SetMovableVisualPointLocationByLabel("Lower limit", new PointXY((Double) source.getValue(), f.Calc((Double) source.getValue())));
 		}
 		else if(source.getName().equalsIgnoreCase("upX"))
 		{
 			if((Double) source.getValue() < (Double) x1.getValue())
 			{
 				source.setValue(x1.getValue());
 			}
 			else
 				GraphFunctionsFrame.gframe.SetMovableVisualPointLocationByLabel("Upper limit", new PointXY((Double) source.getValue(), f.Calc((Double) source.getValue())));
 			
 		}
 		
 	}
 	
 	
 	
 }
