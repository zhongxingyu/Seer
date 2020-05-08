 import java.util.ResourceBundle;
 
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QIntValidator;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QValidator;
 import com.trolltech.qt.gui.QWidget;
 
 
 public class SetupScreen2 extends QWidget {
 	
 	QWidget parent;
 	ResourceBundle bundle;
 	GameBoard gb;
 	QLineEdit[] lengths;
 	
 	public SetupScreen2(QWidget parent, ResourceBundle bundle, GameBoard gb) {
 		super(parent);
 		this.bundle = bundle;
 		this.parent = parent;
 		this.parent.setWindowTitle(bundle.getString("setupScreen") + "2");
 		
 		this.gb = gb;
 		
 		QWidget menu = createMenu();
 		
 		QVBoxLayout widgetLayout = new QVBoxLayout();
 		widgetLayout.addWidget(menu);
 		
 		this.setLayout(widgetLayout);
 		
 		this.show();
 	}
 	
 	public QWidget createMenu() {
 		QWidget menu = new QWidget(this);
 		
 		QGridLayout menuLayout = new QGridLayout();
 		QValidator intValidator = new QIntValidator(this);
 		lengths = new QLineEdit[gb.getNumberOfShips()];
 		
 		for(int i = 0; i < gb.getNumberOfShips(); i++) {
 			QLabel shipLength = new QLabel(bundle.getString("shipLength") + (i + 1) + ": ");
 			lengths[i] = new QLineEdit("2");
 			lengths[i].setValidator(intValidator);
 			menuLayout.addWidget(shipLength, i + 1, 1);
 			menuLayout.addWidget(lengths[i], i + 1, 2);
 		}
 		
 		QPushButton back = new QPushButton(bundle.getString("back"));
 		QPushButton next = new QPushButton(bundle.getString("next"));
 		
 		menuLayout.addWidget(back, gb.getNumberOfShips() + 1, 1);
 		menuLayout.addWidget(next, gb.getNumberOfShips() + 1, 2);
 		
 		back.clicked.connect(this.parent, "showSetupScreen1()");
 		next.clicked.connect(this, "showPlaceShipScreen()");
 		
 		menu.setLayout(menuLayout);
 		
 		return menu;
 	}
 	
 	public void showPlaceShipScreen(){
 		int[] lengths2 = new int[gb.getNumberOfShips()];
 		for(int x=0;x<gb.getNumberOfShips();x++){
 			lengths2[x] = (new Integer(lengths[x].text()));
 		}
 		((GameStarter) this.parent).showPlaceShipScreen(gb,lengths2);
 	}
 }
 
