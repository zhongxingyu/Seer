 package ro.finsiel.eunis.factsheet;
 
 import com.lowagie.text.*;
 import com.lowagie.text.Font;
 import ro.finsiel.eunis.WebContentManagement;
 import ro.finsiel.eunis.exceptions.InitializationException;
 import ro.finsiel.eunis.factsheet.habitats.DescriptionWrapper;
 import ro.finsiel.eunis.factsheet.habitats.HabitatFactsheetRelWrapper;
 import ro.finsiel.eunis.factsheet.habitats.HabitatsFactsheet;
 import ro.finsiel.eunis.factsheet.habitats.SyntaxaWrapper;
 import ro.finsiel.eunis.factsheet.species.SpeciesFactsheet;
 import ro.finsiel.eunis.jrfTables.Chm62edtHabitatInternationalNamePersist;
 import ro.finsiel.eunis.jrfTables.HabitatOtherInfo;
 import ro.finsiel.eunis.jrfTables.habitats.factsheet.HabitatCountryPersist;
 import ro.finsiel.eunis.jrfTables.habitats.factsheet.HabitatLegalPersist;
 import ro.finsiel.eunis.jrfTables.habitats.factsheet.OtherClassificationPersist;
 import ro.finsiel.eunis.jrfTables.species.factsheet.SitesByNatureObjectDomain;
 import ro.finsiel.eunis.jrfTables.species.factsheet.SitesByNatureObjectPersist;
 import ro.finsiel.eunis.reports.pdfReport;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.sites.SitesSearchUtility;
 import ro.finsiel.eunis.search.species.factsheet.HabitatsSpeciesWrapper;
 
 import java.awt.*;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * Created by IntelliJ IDEA.
  * User: cromanescu
  * Date: Aug 15, 2005
  * Time: 2:10:45 PM
  */
 public class PDFHabitatsFactsheet
 {
   private static final float TABLE_WIDTH = 94f;
 
   private HabitatsFactsheet factsheet = null;
   private WebContentManagement cm = null;
   private pdfReport report = null;
 
   final Font fontNormal = FontFactory.getFont( FontFactory.HELVETICA, 9, Font.NORMAL );
   final Font fontNormalBold = FontFactory.getFont( FontFactory.HELVETICA, 9, Font.BOLD );
   final Font fontTitle = FontFactory.getFont( FontFactory.HELVETICA, 12, Font.BOLD );
   final Font fontSubtitle = FontFactory.getFont( FontFactory.HELVETICA, 10, Font.BOLD );
 
   /**
    * Contructor for PDFHabitatsFactsheet object.
    * @param idHabitat habitat id
    * @param report Report to write to, already initialized
    * @param contentManagement current content management
    */
   public PDFHabitatsFactsheet( String idHabitat, pdfReport report, WebContentManagement contentManagement )
   {
     this.report = report;
     this.cm = contentManagement;
     factsheet = new HabitatsFactsheet( idHabitat );
   }
 
   /**
    * Generate entire factsheet.
    * @return operation status
    */
   public final boolean generateFactsheet()
   {
     boolean ret = true;
     try
     {
       getGeneralInformation();
       report.getDocument().newPage();
 
       getGeographicalDistribution();
       report.getDocument().newPage();
 
       getLegalInstruments();
       report.getDocument().newPage();
 
       getRelatedHabitats();
       report.getDocument().newPage();
 
       getRelatedSpecies();
       report.getDocument().newPage();
 
       getRelatedSites();
       report.getDocument().newPage();
 
       getOtherInfo();
       report.getDocument().newPage();
     }
     catch ( Exception ex )
     {
       ret = false;
       ex.printStackTrace();
     }
     return ret;
   }
 
   /**
    * habitats-factsheet-general.jsp
    *
    * @throws Exception
    */
   private void getGeneralInformation() throws Exception
   {
 
     Table table = new Table( 1 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
 
     String code = factsheet.getCode2000();
     if ( factsheet.isEunis() )
     {
       code = factsheet.getEunisHabitatCode();
     }
 
     String habitatType = cm.cms( "annex_habitat_type");
     if ( factsheet.isEunis() )
     {
       habitatType = cm.cms( "eunis_habitat_type");
     }
 
     Cell cell;
     cell = new Cell( new Phrase( factsheet.getHabitatScientificName(), fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( "( " + habitatType + " - " + code + " )", fontNormalBold ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     cell.setHorizontalAlignment( Cell.ALIGN_CENTER );
     table.addCell( cell );
     report.addTable( table );
 
     table = new Table( 2 );
     table.setCellsFitPage( true );
     table.setWidth( TABLE_WIDTH );
     table.setAlignment( Table.ALIGN_LEFT );
     table.setBorderWidth( 1 );
     table.setDefaultCellBorderWidth( 1 );
     table.setBorderColor( Color.BLACK );
     table.setCellspacing( 2 );
     float[] colWidths = { 30, 70 };
     table.setWidths( colWidths );
 
     // Name
     cell = new Cell( new Phrase( cm.cms( "habitat_type"), fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     table.addCell( cell );
 
     cell = new Cell( new Phrase( factsheet.getHabitatDescription(), fontTitle ) );
     cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
     table.addCell( cell );
     report.addTable( table );
 
     // Code / level / priority etc.
     if ( factsheet.isEunis() )
     {
       table = new Table( 2 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
       float[] colWidths1 = { 40, 60 };
       table.setWidths( colWidths1 );
 
 
       cell = new Cell( new Phrase( cm.cms( "eunis_habitat_type_code"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( Utilities.formatString( factsheet.getEunisHabitatCode(), " " ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "generic_index_07"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( Utilities.formatString( factsheet.getHabitatLevel(), " " ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
       report.addTable( table );
     }
     else
     {
       table = new Table( 4 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
       float[] colWidths1 = { 40, 10, 40, 10 };
       table.setWidths( colWidths1 );
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_12"), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( Utilities.formatString( factsheet.getCode2000(), " " ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( " ", fontNormal ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( " ", fontNormal ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "originally _published_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       String codeStr = Utilities.formatString(
           factsheet.isAnnexI() ? factsheet.getHabitat().getCodeAnnex1() : factsheet.getHabitat().getOriginallyPublishedCode(),
           " " );
       cell = new Cell( new Phrase( codeStr, fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "priority" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
 
       String priority = factsheet.getPriority() != null && 1 == factsheet.getPriority().shortValue() ?
           cm.cms( "yes" ) : cm.cms( "no" );
       cell = new Cell( new Phrase( priority, fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       table.addCell( cell );
       report.addTable( table );
     }
 
     // Habitat type description
     Vector descriptions = new Vector();
     try
     {
       descriptions = factsheet.getDescrOwner();
     }
     catch ( InitializationException e )
     {
       e.printStackTrace();
     }
     if ( descriptions.size() > 0 )
     {
       table = new Table( 1 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       for ( int i = 0; i < descriptions.size(); i++ )
       {
         DescriptionWrapper description = ( DescriptionWrapper ) descriptions.get( i );
         if ( description.getLanguage().equalsIgnoreCase( "english" ) )
         {
           cell = new Cell( new Phrase( cm.cms( "description" ) + "(" + description.getLanguage() + ")", fontTitle ) );
           cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( description.getDescription(), fontNormal ) );
           table.addCell( cell );
 
           if ( !description.getOwnerText().equalsIgnoreCase( "n/a" ) && !description.getOwnerText().equalsIgnoreCase( "" ) )
           {
             cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_16" ), fontNormal ) );
             cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
             table.addCell( cell );
 
             cell = new Cell( new Phrase( description.getOwnerText(), fontNormal ) );
             table.addCell( cell );
           }
           if ( null != description.getIdDc() )
           {
             String textSource = Utilities.formatString( SpeciesFactsheet.getBookAuthorDate( description.getIdDc() ), " " );
             if ( !textSource.equalsIgnoreCase( "" ) )
             {
               cell = new Cell( new Phrase( cm.cms( "source" ), fontNormal ) );
               cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
               table.addCell( cell );
 
               cell = new Cell( new Phrase( textSource, fontNormal ) );
               table.addCell( cell );
             }
           }
         }
       }
       report.addTable( table );
     }
 
     // List of habitats internationals names.
     List names = factsheet.getInternationalNames();
     if ( names.size() > 0 )
     {
       table = new Table( 2 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 70 };
       table.setWidths( colWidths1 );
 
       cell = new Cell( new Phrase( "Name in other languages", fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 2 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "language" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       // List habitats internationals names.
       for ( int i = 0; i < names.size(); i++ )
       {
         Chm62edtHabitatInternationalNamePersist name = ( Chm62edtHabitatInternationalNamePersist ) names.get( i );
 
         cell = new Cell( new Phrase( name.getNameEn(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( name.getName(), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
 
     // Habitat codes in other classifications.
     List otherClassifHabitats = factsheet.getOtherClassifications();
     // Relation with other habitats
     Vector otherHabitats = factsheet.getOtherHabitatsRelations();
     if ( otherClassifHabitats.size() > 0 || otherHabitats.size() > 0 )
     {
       table = new Table( 4 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 20, 30, 20 };
       table.setWidths( colWidths1 );
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_22" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "classification" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "code_column" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "title" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "relation_type" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int j = 0; j < otherClassifHabitats.size(); j++ )
       {
         OtherClassificationPersist otherClassifHabitat = ( OtherClassificationPersist ) otherClassifHabitats.get( j );
 
         cell = new Cell( new Phrase( otherClassifHabitat.getName(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( otherClassifHabitat.getCode(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( otherClassifHabitat.getTitle(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( HabitatsFactsheet.mapHabitatsRelations( otherClassifHabitat.getRelationType() ), fontNormal ) );
         table.addCell( cell );
       }
 
       for ( int i = 0; i < otherHabitats.size(); i++ )
       {
         HabitatFactsheetRelWrapper otherHab = ( HabitatFactsheetRelWrapper ) otherHabitats.get( i );
         String relation = otherHab.getRelation();
 
         cell = new Cell( new Phrase( " ", fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( otherHab.getEunisCode(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( otherHab.getScientificName(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( relation, fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * habitats-factsheet-geographical.jsp
    *
    * @throws Exception
    */
   private void getGeographicalDistribution() throws Exception
   {
     List results = factsheet.getHabitatCountries();
     if ( results.size() > 0 )
     {
       Table table = new Table( 4 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 20, 30, 20, 30 };
       table.setWidths( colWidths1 );
 
       Cell cell;
       cell = new Cell( new Phrase( cm.cms( "geographical_distribution" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "country" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "biogeographic_region" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "probability" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "comment" ), fontNormal ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < results.size(); i++ )
       {
         HabitatCountryPersist country = ( HabitatCountryPersist ) results.get( i );
 
         cell = new Cell( new Phrase( country.getAreaNameEn(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( country.getBiogeoregionName(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( ( HabitatsFactsheet.getProbabilityAndCommentForHabitatGeoscope( country.getIdReportAttributes() ) ).get( 0 ).toString(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( ( HabitatsFactsheet.getProbabilityAndCommentForHabitatGeoscope( country.getIdReportAttributes() ) ).get( 1 ).toString(), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * habitats-factsheet-legal.jsp
    *
    * @throws Exception
    */
   private void getLegalInstruments() throws Exception
   {
     Vector legals = new Vector();
     try
     {
       legals = factsheet.getHabitatLegalInfo();
     }
     catch ( InitializationException e )
     {
       e.printStackTrace();
     }
     if ( ( factsheet.isEunis() && !legals.isEmpty() ) )
     {
       Table table = new Table( 3 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 40, 40, 20 };
       table.setWidths( colWidths1 );
 
       Cell cell;
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_27" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 3 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "legal_instrument" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_29" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_30" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < legals.size(); i++ )
       {
         HabitatLegalPersist legal = ( HabitatLegalPersist ) legals.get( i );
 
         cell = new Cell( new Phrase( legal.getLegalName(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( legal.getTitle(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( legal.getCode(), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   /**
    * habitats-factsheet-other.jsp
    *
    * @throws Exception
    */
   private void getOtherInfo() throws Exception
   {
     Integer[] dictionary = {
         HabitatsFactsheet.OTHER_INFO_ALTITUDE,
         HabitatsFactsheet.OTHER_INFO_DEPTH,
         HabitatsFactsheet.OTHER_INFO_CLIMATE,
         HabitatsFactsheet.OTHER_INFO_GEOMORPH,
         HabitatsFactsheet.OTHER_INFO_SUBSTRATE,
         HabitatsFactsheet.OTHER_INFO_LIFEFORM,
         HabitatsFactsheet.OTHER_INFO_COVER,
         HabitatsFactsheet.OTHER_INFO_HUMIDITY,
         HabitatsFactsheet.OTHER_INFO_WATER,
         HabitatsFactsheet.OTHER_INFO_SALINITY,
         HabitatsFactsheet.OTHER_INFO_EXPOSURE,
         HabitatsFactsheet.OTHER_INFO_CHEMISTRY,
         HabitatsFactsheet.OTHER_INFO_TEMPERATURE,
         HabitatsFactsheet.OTHER_INFO_LIGHT,
         HabitatsFactsheet.OTHER_INFO_SPATIAL,
         HabitatsFactsheet.OTHER_INFO_TEMPORAL,
         HabitatsFactsheet.OTHER_INFO_IMPACT,
         HabitatsFactsheet.OTHER_INFO_USAGE
     };
 
     for ( int i = 0; i < dictionary.length; i++ )
     {
       Integer infoID = dictionary[ i ];
       String title = factsheet.getOtherInfoDescription( infoID );
       List results = new Vector();
       try
       {
         results = factsheet.getOtherInfo( infoID );
       }
       catch ( Exception ex )
       {
         ex.printStackTrace();
       }
       if ( !results.isEmpty() )
       {
         Table table = new Table( 2 );
         table.setCellsFitPage( true );
         table.setWidth( TABLE_WIDTH );
         table.setAlignment( Table.ALIGN_LEFT );
         table.setBorderWidth( 1 );
         table.setDefaultCellBorderWidth( 1 );
         table.setBorderColor( Color.BLACK );
         table.setCellspacing( 2 );
 
         float[] colWidths1 = { 30, 70 };
         table.setWidths( colWidths1 );
 
         Cell cell;
         cell = new Cell( new Phrase( title, fontSubtitle ) );
         cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
         cell.setColspan( 2 );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( cm.cms( "name" ), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( cm.cms( "description" ), fontNormalBold ) );
         cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
         table.addCell( cell );
 
         for ( int ii = 0; ii < results.size(); ii++ )
         {
           HabitatOtherInfo obj = ( HabitatOtherInfo ) results.get( ii );
           String name = ( null == obj.getName() ) ? "n/a" : obj.getName();
           String description = ( null == obj.getDescription() ) ? "n/a" : obj.getDescription();
 
           cell = new Cell( new Phrase( name, fontNormal ) );
           table.addCell( cell );
 
           cell = new Cell( new Phrase( description, fontNormal ) );
           table.addCell( cell );
         }
         report.addTable( table );
       }
     }
   }
 
   /**
    * Generate habitats-factsheet-related.jsp equivalent.
    * @throws Exception When error occurrs
    */
   private void getRelatedHabitats() throws Exception
   {
     Vector syntaxaw = new Vector();
     try
     {
       syntaxaw = factsheet.getHabitatSintaxa();
     }
     catch ( InitializationException e )
     {
       e.printStackTrace();
     }
     if ( !syntaxaw.isEmpty() )
     {
       Table table = new Table( 5 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 20, 30, 10, 10 };
       table.setWidths( colWidths1 );
 
       Cell cell;
       cell = new Cell( new Phrase( cm.cms( "habitat_type_syntaxa" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 5 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "relation" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_75" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "author" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "references" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       String IdDc = "";
       for ( int i = 0; i < syntaxaw.size(); i++ )
       {
         SyntaxaWrapper syntaxa = ( SyntaxaWrapper ) syntaxaw.get( i );
 
         cell = new Cell( new Phrase( syntaxa.getName(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( HabitatsFactsheet.mapHabitatsRelations( syntaxa.getRelation() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( syntaxa.getSourceAbbrev(), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( syntaxa.getAuthor(), fontNormal ) );
         table.addCell( cell );
 
         String text = " ";
         if ( syntaxa.getIdDc() != null )
         {
           IdDc = syntaxa.getIdDc().toString();
         }
         if ( !IdDc.equalsIgnoreCase( "0" ) )
         {
           text = Utilities.getAuthorAndUrlByIdDc( IdDc ).get( 0 ).toString();
         }
         cell = new Cell( new Phrase( text, fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   private void getRelatedSites() throws Exception
   {
 
     String isGoodHabitat = " IF(TRIM(A.CODE_2000) <> '',RIGHT(A.CODE_2000,2),1) <> IF(TRIM(A.CODE_2000) <> '','00',2) AND IF(TRIM(A.CODE_2000) <> '',LENGTH(A.CODE_2000),1) = IF(TRIM(A.CODE_2000) <> '',4,1) ";
     // Sites for which this habitat is recorded.
     List sites = new SitesByNatureObjectDomain().findCustom( "SELECT C.ID_SITE, C.NAME, C.SOURCE_DB, C.LATITUDE, C.LONGITUDE, E.AREA_NAME_EN " +
         " FROM CHM62EDT_HABITAT AS A " +
         " INNER JOIN CHM62EDT_NATURE_OBJECT_REPORT_TYPE AS B ON A.ID_NATURE_OBJECT = B.ID_NATURE_OBJECT_LINK " +
         " INNER JOIN CHM62EDT_SITES AS C ON B.ID_NATURE_OBJECT = C.ID_NATURE_OBJECT " +
         " LEFT JOIN CHM62EDT_NATURE_OBJECT_GEOSCOPE AS D ON C.ID_NATURE_OBJECT = D.ID_NATURE_OBJECT " +
         " LEFT JOIN CHM62EDT_COUNTRY AS E ON D.ID_GEOSCOPE = E.ID_GEOSCOPE " +
         " WHERE   " + isGoodHabitat + " AND A.ID_NATURE_OBJECT =" + factsheet.getHabitat().getIdNatureObject() +
         " AND C.SOURCE_DB <> 'EMERALD'" +
         " GROUP BY C.ID_NATURE_OBJECT" );
 
     // Sites for habitat subtypes.
     List sitesForSubtypes = new SitesByNatureObjectDomain().findCustom( "SELECT C.ID_SITE, C.NAME, C.SOURCE_DB, C.LATITUDE, C.LONGITUDE, E.AREA_NAME_EN " +
         " FROM CHM62EDT_HABITAT AS A " +
         " INNER JOIN CHM62EDT_NATURE_OBJECT_REPORT_TYPE AS B ON A.ID_NATURE_OBJECT = B.ID_NATURE_OBJECT_LINK " +
         " INNER JOIN CHM62EDT_SITES AS C ON B.ID_NATURE_OBJECT = C.ID_NATURE_OBJECT " +
         " LEFT JOIN CHM62EDT_NATURE_OBJECT_GEOSCOPE AS D ON C.ID_NATURE_OBJECT = D.ID_NATURE_OBJECT " +
         " LEFT JOIN CHM62EDT_COUNTRY AS E ON D.ID_GEOSCOPE = E.ID_GEOSCOPE " +
         " WHERE A.ID_NATURE_OBJECT =" + factsheet.getHabitat().getIdNatureObject() +
         ( factsheet.isAnnexI() ? " and right(A.code_2000,2) <> '00' and length(A.code_2000) = 4 AND if(right(A.code_2000,1) = '0',left(A.code_2000,3),A.code_2000) like '" + factsheet.getCode2000() + "%' and A.code_2000 <> '" + factsheet.getCode2000() + "'" : " AND A.EUNIS_HABITAT_CODE like '" + factsheet.getEunisHabitatCode() + "%' and A.EUNIS_HABITAT_CODE<> '" + factsheet.getEunisHabitatCode() + "'" ) +
         " AND C.SOURCE_DB <> 'EMERALD'" +
         " GROUP BY C.ID_NATURE_OBJECT" );
     if ( null != sites && !sites.isEmpty() )
     {
       Table table = new Table( 4 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 20, 30, 20 };
       table.setWidths( colWidths1 );
       Cell cell;
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_sitesForHabitatRecorded" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "site_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "source_data_set" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "country" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "site_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       // List of sites for which this habitat is recorded.
       for ( int i = 0; i < sites.size(); i++ )
       {
         SitesByNatureObjectPersist site = ( SitesByNatureObjectPersist ) sites.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( site.getIDSite() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( SitesSearchUtility.translateSourceDB( site.getSourceDB() ) ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( site.getAreaNameEn() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( site.getName() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
 
     if ( sitesForSubtypes.size() > 0 )
     {
       Table table = new Table( 4 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 30, 20, 30, 20 };
       table.setWidths( colWidths1 );
       Cell cell;
 
       cell = new Cell( new Phrase( cm.cms( "habitats_factsheet_sitesForSubtypes" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 4 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "site_code" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "source_data_set" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "country" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "site_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < sitesForSubtypes.size(); i++ )
       {
         SitesByNatureObjectPersist site = ( SitesByNatureObjectPersist ) sitesForSubtypes.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( site.getIDSite() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( SitesSearchUtility.translateSourceDB( site.getSourceDB() ) ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( site.getAreaNameEn() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( site.getName() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
   private void getRelatedSpecies() throws Exception
   {
     List species = factsheet.getSpeciesForHabitats();
     if ( !species.isEmpty() )
     {
       Table table = new Table( 6 );
       table.setCellsFitPage( true );
       table.setWidth( TABLE_WIDTH );
       table.setAlignment( Table.ALIGN_LEFT );
       table.setBorderWidth( 1 );
       table.setDefaultCellBorderWidth( 1 );
       table.setBorderColor( Color.BLACK );
       table.setCellspacing( 2 );
 
       float[] colWidths1 = { 20, 20, 20, 20, 10, 10 };
       table.setWidths( colWidths1 );
       Cell cell;
 
      cell = new Cell( new Phrase( cm.cms( "species_characteristics_for_habitat_type" ), fontSubtitle ) );
       cell.setBackgroundColor( new Color( 0xDD, 0xDD, 0xDD ) );
       cell.setColspan( 6 );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "species_scientific_name" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "biogeographic_region" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "abundance" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "frequencies" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "faithfulness" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       cell = new Cell( new Phrase( cm.cms( "comment" ), fontNormalBold ) );
       cell.setBackgroundColor( new Color( 0xEE, 0xEE, 0xEE ) );
       table.addCell( cell );
 
       for ( int i = 0; i < species.size(); i++ )
       {
         HabitatsSpeciesWrapper wrapper = ( HabitatsSpeciesWrapper ) species.get( i );
 
         cell = new Cell( new Phrase( Utilities.formatString( wrapper.getSpeciesName() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( wrapper.getGeoscope() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( wrapper.getAbundance() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( wrapper.getFrequencies() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( wrapper.getFaithfulness() ), fontNormal ) );
         table.addCell( cell );
 
         cell = new Cell( new Phrase( Utilities.formatString( wrapper.getComment() ), fontNormal ) );
         table.addCell( cell );
       }
       report.addTable( table );
     }
   }
 
 }
