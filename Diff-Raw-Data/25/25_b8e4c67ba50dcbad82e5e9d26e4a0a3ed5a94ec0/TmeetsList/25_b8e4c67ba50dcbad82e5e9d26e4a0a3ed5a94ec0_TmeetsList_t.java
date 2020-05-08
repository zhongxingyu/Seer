 package tmitter.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.rwt.lifecycle.WidgetUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
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
 
 import tmitter.model.Monster;
 import tmitter.model.MonsterUtil;
 import tmitter.model.Status;
 import tmitter.model.StatusUtil;
 
 
 public class TmeetsList {
   
   private Map<Status, Monster> tmeetMap;
   private ArrayList<Monster> monsters;
   private Monster currentMonster;
   private Composite parent;
   private ScrolledComposite container;
   private List<String> allMonsterNames;
   private final String filter;
 
   public TmeetsList( List<String> allMonsterNames, Monster currentMonster, String filter ) {
     this.allMonsterNames = allMonsterNames;
     this.currentMonster = currentMonster;
     this.filter = filter;
     loadTmeets();
   }
   
   public void setMonsters( List<String> allMonsterNames ) {
     this.allMonsterNames = allMonsterNames;
   }
 
   private void loadTmeets() {
     tmeetMap = new HashMap<Status, Monster>();
     monsters = new ArrayList<Monster>();
     for( String monsterName : allMonsterNames ) {
       Monster monster = MonsterUtil.loadMonster( monsterName );
       monsters.add( monster );
     }
     createTmeetsMap();
   }
 
   private void createTmeetsMap() {
     for( Monster monster : monsters ) {
       List<Status> tmeets = monster.getTmeets();
       if( tmeets != null ) {
         for( Status status : tmeets ) {
           if( filter != null ) {
             if( status.getMessage().contains( filter ) ) {
               tmeetMap.put( status, monster );
             }
           } else {
             tmeetMap.put( status, monster );
           }
         }
       }
     }
   }
 
   public void createContols( Composite parent ) {
     this.parent = parent;
     container = new ScrolledComposite( parent, SWT.V_SCROLL );
     Control content = createContent( container );
     container.setContent( content );
     container.setExpandVertical( true );
     container.setExpandHorizontal( true );
     container.setMinSize( content.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
   }
 
   private Control createContent( ScrolledComposite parent ) {
     Composite container = new Composite( parent, SWT.NONE );
     container.setLayout( new GridLayout( 1, true ) );
     addTmeets( container );
     container.layout();
     return container;
   }
 
   private void addTmeets( Composite parent ) {
     List<Status> tmeets = StatusUtil.createStatusUpdateListFromMonsters( monsters );
     for( final Status tmeet : tmeets ) {
       if( tmeetMap.containsKey( tmeet ) ) {
         Composite container = createTmeetContainer( parent );
         Label imageLabel = createTmeetImageLabel( tmeet, container );
         String name = tmeetMap.get( tmeet ).getName();
         String species = tmeetMap.get( tmeet ).getSpecies().toString();
         Label dateLabel = createTmeetDateLabel( tmeet, container, imageLabel, name, species );
         createTmeetTextLabel( tmeet, container, dateLabel );
         if( currentMonster != null ) {
           Button retmeetButton = createRetmeetButton( container, tmeet );
           createFollowingButtons( container, tmeet, retmeetButton );
           if( currentMonster.getName().equals( name ) ) {
             createDeleteButton( tmeet, container, retmeetButton );
           }
         }
       }
     }
   }
 
   private Composite createTmeetContainer( Composite parent ) {
     Composite container = new Composite( parent, SWT.NONE );
     container.setData( WidgetUtil.CUSTOM_VARIANT, "tmeet" );
     container.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
     container.setLayout( new FormLayout() );
     return container;
   }
 
   private Label createTmeetImageLabel( Status tmeet, Composite container ) {
     Label imageLabel = new Label( container, SWT.NONE );
     imageLabel.setData( WidgetUtil.CUSTOM_VARIANT, "timestamp" );
     imageLabel.setImage( ImageUtil.createMonsterImage( tmeetMap.get( tmeet ) ) );
     FormData imageLabelData = new FormData();
     imageLabelData.left = new FormAttachment( 0, 5 );
     imageLabelData.top = new FormAttachment( 0, 5 );
     return imageLabel;
   }
 
   private Label createTmeetDateLabel( Status tmeet,
                                       Composite container,
                                       Label imageLabel,
                                       String name, 
                                       String species )
   {
     Label dateLabel = new Label( container, SWT.NONE );
     dateLabel.setData( WidgetUtil.CUSTOM_VARIANT, "timestamp" );
     dateLabel.setText( name + " (" + species + ") on " + tmeet.getTimestamp().toString() );
     FormData dateLableData = new FormData();
     dateLabel.setLayoutData( dateLableData );
     dateLableData.left = new FormAttachment( imageLabel, 10 );
     dateLableData.top = new FormAttachment( 0, 3 );
     return dateLabel;
   }
 
   private void createTmeetTextLabel( Status tmeet,
                                      Composite container,
                                      Label dateLabel )
   {
     Label textLabel = new Label( container, SWT.NONE );
     textLabel.setData( WidgetUtil.CUSTOM_VARIANT, "tmeet" );
     textLabel.setText( tmeet.getMessage() );
     FormData textLabelData = new FormData();
     textLabel.setLayoutData( textLabelData );
     textLabelData.left = new FormAttachment( 0, 80 );
     textLabelData.top = new FormAttachment( dateLabel, 6 );
   }
 
   private Button createRetmeetButton( Composite container, final Status tmeet ) {
     Button retmeetButton = new Button( container, SWT.PUSH );
     retmeetButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     retmeetButton.setText( "retmeet" );
     FormData retmeetData = new FormData();
     retmeetButton.setLayoutData( retmeetData );
     retmeetData.right = new FormAttachment( 100, -10 );
     retmeetData.bottom = new FormAttachment( 100, 0 );
     retmeetButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       public void widgetSelected( SelectionEvent e ) {
         String name = tmeetMap.get( tmeet ).getName();
         currentMonster.updateStatus( "RT @" + name + " " + tmeet.getMessage() );
         currentMonster.save();
         refresh( currentMonster );
       }
     } );
     return retmeetButton;
   }
 
   private void createFollowingButtons( Composite container,
                                    Status tmeet,
                                    Button retmeetButton )
   {
     final String name = tmeetMap.get( tmeet ).getName();
    List<String> watchedMonsters = currentMonster.getWatchedMonsters();
    if(    watchedMonsters != null 
        && !watchedMonsters.contains( name ) 
         && !name.equals( currentMonster.getName() ) ) 
     {
       createFollowButton( container, retmeetButton, name );
     } else if( !name.equals( currentMonster.getName() ) ) {
       createUnfollowButton( container, retmeetButton, name );
     }
   }
 
   private void createFollowButton( Composite container,
                                    Button retmeetButton,
                                    final String name )
   {
     Button followButton = new Button( container, SWT.PUSH );
     followButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     followButton.setText( "follow" );
     FormData followData = new FormData();
     followButton.setLayoutData( followData );
     followData.right = new FormAttachment( retmeetButton, -10 );
     followData.bottom = new FormAttachment( 100, 0 );
     followButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       public void widgetSelected( SelectionEvent e ) {
         currentMonster.follow( name );
         currentMonster.save();
         refresh( currentMonster );
       }
     } );
   }
 
   private void createUnfollowButton( Composite container,
                                      Button retmeetButton,
                                      final String name )
   {
     Button unfollowButton = new Button( container, SWT.PUSH );
     unfollowButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     unfollowButton.setText( "unfollow" );
     FormData followData = new FormData();
     unfollowButton.setLayoutData( followData );
     followData.right = new FormAttachment( retmeetButton, -10 );
     followData.bottom = new FormAttachment( 100, 0 );
     unfollowButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
 
       public void widgetSelected( SelectionEvent e ) {
         currentMonster.unfollow( name );
         currentMonster.save();
         refresh( currentMonster );
       }
     } );
   }
 
   private void createDeleteButton( final Status tmeet,
                                    Composite container,
                                    Button retmeetButton )
   {
     Button deleteButton = new Button( container, SWT.PUSH );
     deleteButton.setData( WidgetUtil.CUSTOM_VARIANT, "login" );
     deleteButton.setText( "delete" );
     FormData deleteData = new FormData();
     deleteButton.setLayoutData( deleteData );
     deleteData.right = new FormAttachment( retmeetButton, -10 );
     deleteData.bottom = new FormAttachment( 100, 0 );
     deleteButton.addSelectionListener( new SelectionAdapter() {
       private static final long serialVersionUID = 1L;
   
       public void widgetSelected( SelectionEvent e ) {
         currentMonster.deleteStatus( tmeet );
         currentMonster.save();
         refresh( currentMonster );
       }
     } );
   }
 
   public void refresh( Monster currentMonster ) {
     this.currentMonster = currentMonster;
     container.dispose();
     container = null;
     loadTmeets();
     createContols( parent );
     if( parent != null && !parent.isDisposed() ) {
       parent.layout( true, true );
     }
   }
 
 }
