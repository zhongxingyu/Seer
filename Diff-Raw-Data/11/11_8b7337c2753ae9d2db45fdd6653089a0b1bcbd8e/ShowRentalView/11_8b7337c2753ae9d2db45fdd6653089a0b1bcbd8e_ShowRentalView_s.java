 package at.owlsoft.owlet.ui;
 
 import java.net.URL;
 
 import org.apache.pivot.collections.ArrayList;
 import org.apache.pivot.collections.List;
 import org.apache.pivot.collections.Map;
 import org.apache.pivot.util.Resources;
 import org.apache.pivot.wtk.BoxPane;
 import org.apache.pivot.wtk.Button;
 import org.apache.pivot.wtk.ButtonPressListener;
 import org.apache.pivot.wtk.Label;
 import org.apache.pivot.wtk.ListButton;
 import org.apache.pivot.wtk.ListButtonSelectionListener;
 import org.apache.pivot.wtk.Prompt;
 
 import at.owlsoft.owl.model.accounting.IFilingExtension;
 import at.owlsoft.owl.model.accounting.IRental;
 import at.owlsoft.owlet.viewmodel.ShowRentalViewModel;
 
 public class ShowRentalView extends OwletView
 {
     private ShowRentalViewModel _viewModel;
     private BoxPane             _userPane;
     private BoxPane             _exemplarPane;
     private BoxPane             _historyPane;
     private BoxPane             _extensionPane;
     private Label               _userFirstName;
     private Label               _userLastName;
     private Button              _loadDefaultUserButton;
     private Button              _doExtensionButton;
     private Label               _exemplarID;
     private Label               _exemplarName;
     private Label               _extensionCount;
     private ListButton          _allRentalsListButton;
 
     public ShowRentalView()
     {
         _viewModel = new ShowRentalViewModel();
     }
 
     @Override
     public void initialize(Map<String, Object> ns, URL arg1, Resources arg2)
     {
         setEnabled(true);
 
         _exemplarPane = (BoxPane) ns.get("exemplarPane");
         _userPane = (BoxPane) ns.get("userPane");
         _historyPane = (BoxPane) ns.get("historyPane");
         _extensionPane = (BoxPane) ns.get("extensionPane");
         _userFirstName = (Label) ns.get("userFirstName");
         _userLastName = (Label) ns.get("userLastName");
         _exemplarID = (Label) ns.get("exemplarID");
         _exemplarName = (Label) ns.get("exemplarName");
         _extensionCount = (Label) ns.get("extensionCount");
         _allRentalsListButton = (ListButton) ns.get("allRentalsListButton");
 
         Button _createNewExtensionButton = (Button) ns
                 .get("createNewExtensionButton");
         _createNewExtensionButton.getButtonPressListeners().add(
                 new ButtonPressListener()
                 {
                     @Override
                     public void buttonPressed(Button arg0)
                     {
                         _viewModel.createNewExtension(_viewModel
                                 .getActiveRental());
                         setRentalData();
 
                     }
                 });
 
         Button _returnRentalButton = (Button) ns.get("returnRentalButton");
         _returnRentalButton.getButtonPressListeners().add(
                 new ButtonPressListener()
                 {
                     @Override
                     public void buttonPressed(Button arg0)
                     {
                         _viewModel.returnRental(_viewModel.getActiveRental());
 
                     }
                 });
 
         _allRentalsListButton.getListButtonSelectionListeners().add(
                 new ListButtonSelectionListener.Adapter()
                 {
 
                     @Override
                     public void selectedItemChanged(ListButton arg0,
                             Object previousItem)
                     {
                         _viewModel.setActiveRental((IRental) arg0
                                 .getSelectedItem());
                         setRentalData();
 
                     }
                 });
 
         Button _doExtensionButton = (Button) ns.get("doExtensionButton");
         _doExtensionButton.getButtonPressListeners().add(
                 new ButtonPressListener()
                 {
                     @Override
                     public void buttonPressed(Button arg0)
                     {
 
                     }
                 });
 
         _loadDefaultUserButton = (Button) ns.get("loadDefaultUserButton");
         _loadDefaultUserButton.getButtonPressListeners().add(
                 new ButtonPressListener()
                 {
                     @Override
                     public void buttonPressed(Button arg0)
                     {
                         setUserData();
                     }
                 });
 
     }
 
     private void setUserData()
     {
         _userFirstName.setText(_viewModel.getSystemUser().getFirstName());
         _userLastName.setText(_viewModel.getSystemUser().getLastName());
         _allRentalsListButton.setListData(_viewModel.getRentals());
     }
 
     private void setRentalData()
     {
         List<IFilingExtension> extensions = new ArrayList<IFilingExtension>();
         _viewModel.getActiveRental().getMediumExemplar();
         _viewModel.getActiveRental().getFilingExtensions();
         _exemplarID.setText(new Integer(_viewModel.getActiveRental()
                 .getMediumExemplar().getExemplarID()).toString());
 
         _exemplarName.setText(_viewModel.getActiveRental().getMediumExemplar()
                 .getMedium().getName());
 
         _extensionCount.setText(new Integer(_viewModel.getActiveRental()
                 .getFilingExtensionCount()).toString());
 
     }
 
     @Override
     protected void onViewOpened()
     {
         try
         {
             _viewModel.initialize();
         }
         catch (Exception e)
         {
             Prompt.prompt(e.getMessage(), getWindow());
             setEnabled(false);
         }
 
     }
 }
