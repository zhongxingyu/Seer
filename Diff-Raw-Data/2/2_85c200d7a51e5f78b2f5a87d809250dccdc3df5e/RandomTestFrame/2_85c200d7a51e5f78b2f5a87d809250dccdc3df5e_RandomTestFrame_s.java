 package gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import ast.ArrayType;
 import ast.BooleanType;
 import ast.FunctionParameter;
 import ast.IntegerType;
 import ast.Type;
 
 /**
  * This class represents a frame for random tests.
  */
 public class RandomTestFrame extends Frame implements MiscFrame {
 	/**
 	 * shell of the frame
 	 */
 	private Shell shell;
 	/**
 	 * text field for the number of random tests
 	 */
 	private Text count;
 	/**
 	 * text fields for intervals and array sizes
 	 */
 	private Text[][] intervals;
 	/**
 	 * button for starting execution
 	 */
 	private Button startButton;
 	
 	/**
 	 * Construct a random test frame with the specified parent shell.
 	 * @param parentShell specified shell
 	 */
 	public RandomTestFrame(Shell parentShell) {
 		this.shell = new Shell(parentShell, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
 		this.shell.setText("Random Test");
 		
 		GridLayout gLayout = new GridLayout();
 		gLayout.numColumns = 4;
 		gLayout.makeColumnsEqualWidth = true;
 		gLayout.marginTop = 10;
 		gLayout.marginLeft = 10;
 		gLayout.marginRight = 10;
 		shell.setLayout(gLayout);
 	}
 	
 	/**
 	 * Add attributes to the shell of the frame and open it. The size
 	 * of the shell depends on the number of parameters.
 	 * @param parameters list of parameters
 	 */
 	public void createFrame(FunctionParameter[] parameters) {
 		//Add various components
 		Label label = new Label(this.shell, SWT.NONE);
 		label.setText("Number of random tests: ");	
 		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 2;
 		label.setLayoutData(gData);
 		this.count = new Text(this.shell, SWT.NONE);
 		gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 2;
 		this.count.setLayoutData(gData);
 		Label label2 = new Label(this.shell, SWT.NONE);
 		gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 4;
 		label2.setLayoutData(gData);
 		new Label(this.shell, SWT.NONE).setText("ID");
 		new Label(this.shell, SWT.NONE).setText("Begin");
 		new Label(this.shell, SWT.NONE).setText("End");
 		new Label(this.shell, SWT.NONE).setText("Array size");
 
 		//Add text fields for interval and array size depending on the parameter type
 		this.intervals = new Text[parameters.length][3];
 		int i;
 		for (i = 0; i < parameters.length; i++) {
 			new Label(this.shell, SWT.NONE).setText(parameters[i].toString());
 			Text begin = new Text(this.shell, SWT.NONE);
 			gData = new GridData(GridData.FILL_HORIZONTAL);
 			begin.setLayoutData(gData);	
 			Text end = new Text(this.shell, SWT.NONE);
 			gData = new GridData(GridData.FILL_HORIZONTAL);
 			end.setLayoutData(gData);	
 			Text size = new Text(this.shell, SWT.NONE);
 			gData = new GridData(GridData.FILL_HORIZONTAL);
 			size.setLayoutData(gData);
 			
 			if (parameters[i].getType() instanceof IntegerType) {
 				size.setEditable(false);
 				size.setBackground(new Color(this.shell.getDisplay(), 210, 210, 210));
 			}
 			else if (parameters[i].getType() instanceof BooleanType) {
 				begin.setEditable(false);
 				begin.setBackground(new Color(this.shell.getDisplay(), 210, 210, 210));
 				end.setEditable(false);
 				end.setBackground(new Color(this.shell.getDisplay(), 210, 210, 210));
 				size.setEditable(false);
 				size.setBackground(new Color(this.shell.getDisplay(), 210, 210, 210));
 			}
 			else if (parameters[i].getType() instanceof ArrayType) {
 				Type tmp = parameters[i].getType();
 				while (tmp instanceof ArrayType) {
 					tmp = ((ArrayType) tmp).getType();
 				}
 				if (tmp instanceof BooleanType) {
 					begin.setEditable(false);
 					begin.setBackground(new Color(this.shell.getDisplay(), 210, 210, 210));
 					end.setEditable(false);
 					end.setBackground(new Color(this.shell.getDisplay(), 210, 210, 210));
 				}
 			}
 			
 			this.intervals[i][0] = begin; 
 			this.intervals[i][1] = end;
 			this.intervals[i][2] = size;
 		}
 		
 		Label label3 = new Label(this.shell, SWT.NONE);
 		gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 4;
 		label3.setLayoutData(gData);
 		this.startButton = new Button(this.shell, SWT.NONE);
 		this.startButton.setText("Start");
 		gData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
 		gData.horizontalSpan = 4;
 		this.startButton.setLayoutData(gData);
 		
 		this.shell.setSize(400, 180 + 20 * i);
 		this.shell.open();
 	}
 	
 	/**
 	 * Open an empty shell with the specified text.
 	 * @param s specified text
 	 */
 	public void createEmptyFrame(String s) {
 		Label label = new Label(this.shell, SWT.NONE);
 		label.setText(s);
 		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 5;
 		label.setForeground(new Color(this.shell.getDisplay(), 150, 0, 0));
 		label.setLayoutData(gData);
		this.shell.setSize(260, 80);
 		this.shell.open();
 	}
 	
 	/**
 	 * Return the shell of the frame.
 	 * @return shell of the frame
 	 */
 	public Shell getShell() {
 		return this.shell;
 	}
 	
 	/**
 	 * Return the text field for the number of random tests
 	 * @return text field for the number of random tests
 	 */
 	public Text getCount() {
 		return this.count;
 	}
 	
 	/**
 	 * Return the text fields for the intervals and array sizes
 	 * @return text fields for the intervals and array sizes
 	 */
 	public Text[][] getIntervals() {
 		return this.intervals;
 	}
 	
 	/**
 	 * Return the start button for the random test.
 	 * @return start button
 	 */
 	public Button getStartButton() {
 		return this.startButton;
 	}
 	
 	@Override
 	public Button getSaveButton() {
 		return null;
 	}	
 	@Override
 	public void close() {
 		this.shell.close();
 	}
 	@Override
 	public Button getCloseButton() {
 		return null;
 	}
 }
