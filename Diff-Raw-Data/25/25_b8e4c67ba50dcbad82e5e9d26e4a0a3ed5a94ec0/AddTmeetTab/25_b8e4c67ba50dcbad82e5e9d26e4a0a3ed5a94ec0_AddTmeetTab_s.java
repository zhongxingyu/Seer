 package tmitter.ui.add;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.rwt.lifecycle.WidgetUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import tmitter.model.Monster;
 import tmitter.ui.TmeetsList;
 
 import com.codeaffine.example.rwt.osgi.ui.platform.ServiceProvider;
 import com.codeaffine.example.rwt.osgi.ui.platform.UIContributor;
 
 
 public class AddTmeetTab implements UIContributor {
 
   private ServiceProvider serviceProvider;
   private Text inputText;
   private Label countLabel;
   private Monster monster;
   private TmeetsList tmeetList;
 
   @Override
   public String getId() {
     return "Add Tmeet";
   }
 
   void setServiceProvider( ServiceProvider serviceProvider ) {
     this.serviceProvider = serviceProvider;
   }
 
   @Override
   public Control contribute( Composite parent ) {
     Composite container = new Composite( parent, SWT.NONE );
     container.setLayout( new FormLayout() );
     Label title = createTitleLabel( container );
     createCountLabel( container, title );
     FormData inputTextData = createIntputText( container, title );
     Button tmeetButton = createTmeetButton( container, title, inputTextData );
     Composite tmeetListContainer = createTmeetListContainer( container, tmeetButton );
     createTmeetList( tmeetListContainer );
     inputText.setFocus();
     return container;
   }
 
   private Label createTitleLabel( Composite container ) {
     Label title = new Label( container, SWT.NONE );
     title.setData( WidgetUtil.CUSTOM_VARIANT, "update" );
     title.setText( "Tell others what you are doing..." );
     FormData titleData = new FormData();
     title.setLayoutData( titleData );
     titleData.left = new FormAttachment( 0, 5 );
     titleData.top = new FormAttachment( 0, 5 );
     return title;
   }
 
   private void createCountLabel( Composite container, Label title ) {
     countLabel = new Label( container, SWT.NONE );
     countLabel.setData( WidgetUtil.CUSTOM_VARIANT, "update" );
     countLabel.setText( "142 monster characters left" );
     FormData countLabelData = new FormData();
     countLabel.setLayoutData( countLabelData );
     countLabelData.left = new FormAttachment( title, 25 );
     countLabelData.top = new FormAttachment( 0, 5 );
   }
 
   private FormData createIntputText( Composite container, Label title ) {
     inputText = new Text( container, SWT.SINGLE );
     inputText.setData( WidgetUtil.CUSTOM_VARIANT, "update" );
     FormData inputTextData = new FormData();
     inputText.setLayoutData( inputTextData );
     inputTextData.left = new FormAttachment( 0, 5 );
     inputTextData.top = new FormAttachment( title, 5 );
     inputText.addModifyListener( new ModifyListener() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void modifyText( ModifyEvent event ) {
         String text = inputText.getText();
         int length = text.length();
         if( length > 142 ) {
           inputText.setText( text.substring( 0, 141 ) );
         }
         int charactersLeft = 142 - length;
         countLabel.setText( charactersLeft + " monster characters left" );
       }
     } );
     inputText.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       public void widgetDefaultSelected( SelectionEvent e ) {
         addTmeet();
       }
     } );
     return inputTextData;
   }
 
   private Button createTmeetButton( Composite container,
                                     Label title,
                                     FormData inputTextData )
   {
     Button tmeetButton = new Button( container, SWT.NONE );
     tmeetButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     tmeetButton.setText( "Share" );
     FormData tmeetButtonData = new FormData();
     tmeetButton.setLayoutData( tmeetButtonData );
     tmeetButtonData.right = new FormAttachment( 100, -5 );
     tmeetButtonData.top = new FormAttachment( title, 7 );
     inputTextData.right = new FormAttachment( tmeetButton, -5 );
     tmeetButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       public void widgetSelected( SelectionEvent e ) {
         addTmeet();
       }
     } );
     return tmeetButton;
   }
 
   private Composite createTmeetListContainer( Composite container,
                                               Button tmeetButton )
   {
     Composite tmeetListContainer = new Composite( container, SWT.NONE );
     FormData tmeetListContainerData = new FormData();
     tmeetListContainer.setLayoutData( tmeetListContainerData );
     tmeetListContainerData.left = new FormAttachment( 0, 5 );
     tmeetListContainerData.top = new FormAttachment( tmeetButton, 15 );
     tmeetListContainerData.right = new FormAttachment( 100, -5 );
     tmeetListContainerData.bottom = new FormAttachment( 100, -5 );
     tmeetListContainer.setLayout( new FillLayout() );
     return tmeetListContainer;
   }
 
   private void createTmeetList( Composite tmeetListContainer ) {
     monster = serviceProvider.get( Monster.class );
     if( monster != null ) {
      List<String> watchedMonsters = new ArrayList<String>( monster.getWatchedMonsters() );
       watchedMonsters.add( monster.getName() );
       tmeetList = new TmeetsList( watchedMonsters, monster, null );
       tmeetList.createContols( tmeetListContainer );
     }
   }
 
   protected void addTmeet() {
     String text = inputText.getText();
     monster.updateStatus( text );
     monster.save();
     inputText.setText( "" );
     refreshList();
   }
 
   private void refreshList() {
     List<String> watchedMonsters = new ArrayList<String>( monster.getWatchedMonsters() );
     watchedMonsters.add( monster.getName() );
     tmeetList.setMonsters( watchedMonsters );
     tmeetList.refresh( monster );
   }
 }
