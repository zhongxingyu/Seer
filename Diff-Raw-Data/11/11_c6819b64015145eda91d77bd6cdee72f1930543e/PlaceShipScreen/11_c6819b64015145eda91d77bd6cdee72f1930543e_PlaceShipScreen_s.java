 
 import java.util.ResourceBundle;
 
 import com.trolltech.qt.core.QRect;
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QTableWidget;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 
 public class PlaceShipScreen extends QWidget {
 	
 	QWidget parent;
 	ResourceBundle bundle;
 	GameBoard gb;
 	int[] lengths;
 	
 	public PlaceShipScreen(QWidget parent, ResourceBundle bundle, GameBoard gb, int[] lengths) {
 		super(parent);
 		this.bundle = bundle;
 		this.lengths = lengths;
 		this.parent = parent;
 		this.parent.setWindowTitle(bundle.getString("placeShipScreen"));
 		
 		this.gb = gb;
 		
 		QTableWidget table = createTable();
 		
 		QGridLayout widgetLayout = new QGridLayout();
 		widgetLayout.addWidget(table, 1, 2);
 		
 		QPushButton back = new QPushButton(bundle.getString("back"));
 		QPushButton play = new QPushButton(bundle.getString("play"));
 		
 		widgetLayout.addWidget(back, 2, 1);
 		widgetLayout.addWidget(play, 2, 3);
 		
		System.out.println(widgetLayout.columnCount());
		System.out.println(widgetLayout.rowCount());
		
 		back.clicked.connect(this, "showSetupScreen2()");
 		play.clicked.connect(this, "showBoardScreen()");
 		
 		this.setLayout(widgetLayout);
 		table.setFixedSize(500,500);
 		
 		this.show();
 	}
 	
 	public QTableWidget createTable() {
 		QTableWidget table = new QTableWidget(this.gb.getHeight(), gb.getWidth());
 		for (int i = 0; i < table.columnCount(); i++) {
 			table.setColumnWidth(i, (int)(475.0/((double)this.gb.getWidth())));
 		}
 		for (int i = 0; i < table.rowCount(); i++) {
 			table.setRowHeight(i, (int)(475.0/((double)this.gb.getHeight())));
 		}
 		return table;
 	}
 	
 	public void showBoardScreen(){
 		((GameStarter) parent).showBoardScreen(gb, lengths);
 	}
 	
 	public void showSetupScreen2(){
 		((GameStarter) parent).showSetupScreen2(gb);
 	}
 }
