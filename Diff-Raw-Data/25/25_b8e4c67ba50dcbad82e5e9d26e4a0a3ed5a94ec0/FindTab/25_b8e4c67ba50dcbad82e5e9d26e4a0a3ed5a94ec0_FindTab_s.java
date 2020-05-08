 package tmitter.ui.find;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.rwt.lifecycle.WidgetUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import tmitter.model.Monster;
 import tmitter.model.MonsterUtil;
 import tmitter.ui.ImageUtil;
 
 import com.codeaffine.example.rwt.osgi.ui.platform.ServiceProvider;
 import com.codeaffine.example.rwt.osgi.ui.platform.UIContributor;
 
 
 public class FindTab implements UIContributor {
 
   private ServiceProvider serviceProvider;
   private Text inputText;
   private Composite tmeetListContainer;
 
   @Override
   public String getId() {
     return "Find Others";
   }
   
   void setServiceProvider( ServiceProvider serviceProvider ) {
     this.serviceProvider = serviceProvider;
   }
 
   @Override
   public Control contribute( Composite parent ) {
     Composite container = new Composite( parent, SWT.NONE );
     container.setLayout( new FormLayout() );
     Label title = createTitleLabel( container );
     FormData inputTextData = createIntputText( container, title );
     Button tmeetButton = createSearchButton( container, title, inputTextData );
     createTmeetListContainer( container, tmeetButton );
     inputText.setFocus();
     return container;
   }
   
   private Label createTitleLabel( Composite container ) {
     Label title = new Label( container, SWT.NONE );
     title.setData( WidgetUtil.CUSTOM_VARIANT, "update" );
     title.setText( "Find fellow monsters..." );
     FormData titleData = new FormData();
     title.setLayoutData( titleData );
     titleData.left = new FormAttachment( 0, 5 );
     titleData.top = new FormAttachment( 0, 5 );
     return title;
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
         search( inputText.getText() );
       }
     } );
     return inputTextData;
   }
   
   private Button createSearchButton( Composite container,
                                     Label title,
                                     FormData inputTextData )
   {
     Button tmeetButton = new Button( container, SWT.NONE );
     tmeetButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     tmeetButton.setText( "Find" );
     FormData tmeetButtonData = new FormData();
     tmeetButton.setLayoutData( tmeetButtonData );
     tmeetButtonData.right = new FormAttachment( 100, -5 );
     tmeetButtonData.top = new FormAttachment( title, 7 );
     inputTextData.right = new FormAttachment( tmeetButton, -5 );
     tmeetButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       public void widgetSelected( SelectionEvent e ) {
         search( inputText.getText() );
       }
     } );
     return tmeetButton;
   }
   
   private Composite createTmeetListContainer( Composite container,
                                               Button tmeetButton )
   {
     tmeetListContainer = new Composite( container, SWT.NONE );
     tmeetListContainer.setLayout( new GridLayout( 1, false ) );
     FormData tmeetListContainerData = new FormData();
     tmeetListContainer.setLayoutData( tmeetListContainerData );
     tmeetListContainerData.left = new FormAttachment( 0, 5 );
     tmeetListContainerData.top = new FormAttachment( tmeetButton, 15 );
     tmeetListContainerData.right = new FormAttachment( 100, -5 );
     tmeetListContainerData.bottom = new FormAttachment( 100, -5 );
     return tmeetListContainer;
   }
 
   protected void search( String text ) {
     Control[] children = tmeetListContainer.getChildren();
     for( Control child : children ) {
       child.dispose();
     }
     String searchTerm = inputText.getText();
     List<String> allMonsterNames = MonsterUtil.getAllMonsterNames();
     List<Monster> foundMonsters = new ArrayList<Monster>();
     for( String name : allMonsterNames ) {
       if( name.contains( searchTerm ) ) {
         foundMonsters.add( MonsterUtil.loadMonster( name ) );
       }
     }
     createMonsterControls( foundMonsters );
   }
 
   private void createMonsterControls( List<Monster> foundMonsters ) {
     final Monster currentMonster = serviceProvider.get( Monster.class );
     for( final Monster monster : foundMonsters ) {
       if( !monster.getName().equals( currentMonster.getName() ) ) {
         createMonsterControl( currentMonster, monster );
       }
     }
     tmeetListContainer.layout( true, true );
   }
 
   private void createMonsterControl( final Monster currentMonster,
                                      final Monster monster )
   {
     Composite container = new Composite( tmeetListContainer, SWT.NONE );
     container.setData( WidgetUtil.CUSTOM_VARIANT, "tmeet" );
     container.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
     container.setLayout( new FormLayout() );
     Label imageLabel = createImageLabel( monster, container );
     createNameLabel( monster, container, imageLabel );
     createSpeciesLabel( monster, container, imageLabel );
    if( !currentMonster.getWatchedMonsters().contains( monster.getName() ) ) {
       createFollowButton( currentMonster, monster, container );
     } else {
       createUnfollowButton( currentMonster, monster, container );
     }
   }
 
   private Label createImageLabel( final Monster monster, Composite container ) {
     Label imageLabel = new Label( container, SWT.NONE );
     imageLabel.setData( WidgetUtil.CUSTOM_VARIANT, "tmeet" );
     imageLabel.setImage( ImageUtil.createMonsterImage( monster ) );
     FormData imageLabelData = new FormData();
     imageLabel.setLayoutData( imageLabelData );
     imageLabelData.left = new FormAttachment( 0, 0 );
     imageLabelData.top = new FormAttachment( 0, 0 );
     return imageLabel;
   }
 
   private void createNameLabel( final Monster monster,
                                 Composite container,
                                 Label imageLabel )
   {
     Label nameLabel = new Label( container, SWT.NONE );
     nameLabel.setData( WidgetUtil.CUSTOM_VARIANT, "tmeet" );
     nameLabel.setText( monster.getName() );
     FormData nameLabelData = new FormData();
     nameLabel.setLayoutData( nameLabelData );
     nameLabelData.left = new FormAttachment( imageLabel, 25 );
     nameLabelData.top = new FormAttachment( 0, 5 );
   }
 
   private void createSpeciesLabel( final Monster monster,
                                    Composite container,
                                    Label imageLabel )
   {
     Label speciesLabel = new Label( container, SWT.NONE );
     speciesLabel.setData( WidgetUtil.CUSTOM_VARIANT, "timestamp" );
     speciesLabel.setText( monster.getSpecies().toString() );
     FormData speciesLabelData = new FormData();
     speciesLabel.setLayoutData( speciesLabelData );
     speciesLabelData.left = new FormAttachment( imageLabel, 35 );
     speciesLabelData.top = new FormAttachment( 0, 25 );
   }
 
   private void createFollowButton( final Monster currentMonster,
                                    final Monster monster,
                                    Composite container )
   {
     Button followButton = new Button( container, SWT.NONE );
     followButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     followButton.setText( "Follow" );
     FormData followButtonData = new FormData();
     followButton.setLayoutData( followButtonData );
     followButtonData.right = new FormAttachment( 100, -5 );
     followButtonData.bottom = new FormAttachment( 100, -5 );
     followButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
   
       public void widgetSelected( SelectionEvent e ) {
         currentMonster.follow( monster.getName() );
         currentMonster.save();
         search( inputText.getText() );
       }
     } );
   }
 
   private void createUnfollowButton( final Monster currentMonster,
                                      final Monster monster,
                                      Composite container )
   {
     Button unfollowButton = new Button( container, SWT.NONE );
     unfollowButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     unfollowButton.setText( "Unfollow" );
     FormData unfollowButtonData = new FormData();
     unfollowButton.setLayoutData( unfollowButtonData );
     unfollowButtonData.right = new FormAttachment( 100, -5 );
     unfollowButtonData.bottom = new FormAttachment( 100, -5 );
     unfollowButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
   
       public void widgetSelected( SelectionEvent e ) {
         currentMonster.unfollow( monster.getName() );
         currentMonster.save();
         search( inputText.getText() );
       }
     } );
   }
 
 }
