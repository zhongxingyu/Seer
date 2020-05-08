 import com.aldercape.internal.economics.ApplicationModel;
 import com.aldercape.internal.economics.model.Ledger;
 import com.aldercape.internal.economics.ui.LedgerFrame;
 
 public class Application {
 
 	public static void main(String[] args) {
 		System.setProperty("apple.laf.useScreenMenuBar", "true");

 		LedgerFrame frame = new LedgerFrame(new ApplicationModel(new Ledger()));
 		frame.pack();
 		frame.setVisible(true);
 	}
 }
