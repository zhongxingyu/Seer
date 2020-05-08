 package ro.finsiel.eunis.reports.species.references;
 
 /**
  * Date: May 13, 2003
  * Time: 2:25:52 PM
  */
 
 import ro.finsiel.eunis.exceptions.CriteriaMissingException;
 import ro.finsiel.eunis.exceptions.InitializationException;
 import ro.finsiel.eunis.formBeans.AbstractFormBean;
 import ro.finsiel.eunis.jrfTables.species.references.SpeciesBooksDomain;
 import ro.finsiel.eunis.jrfTables.species.references.SpeciesBooksPersist;
 import ro.finsiel.eunis.reports.AbstractTSVReport;
 import ro.finsiel.eunis.reports.XMLReport;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.search.species.references.ReferencesPaginator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * TSV and XML report generation.
  */
 public class TSVSpeciesReferencesReport extends AbstractTSVReport
 {
   /**
    * Form bean used for search.
    */
   private AbstractFormBean formBean = null;
 
   private boolean showInvalidatedSpecies = false;
 
   /**
    * Constructor.
    * @param sessionID Session ID got from page
    * @param formBean  Form bean queried for output formatting (DB query, sort criterias etc)
    * @param showInvalidatedSpecies Show invalidated species
    */
   public TSVSpeciesReferencesReport(String sessionID, AbstractFormBean formBean, boolean showInvalidatedSpecies) {
     super("SpeciesReferencesReport_" + sessionID + ".tsv");
     this.formBean = formBean;
     this.filename = "SpeciesReferencesReport_" + sessionID + ".tsv";
     xmlreport = new XMLReport("SpeciesReferencesReport_" + sessionID + ".xml");
     this.showInvalidatedSpecies = showInvalidatedSpecies;
     if (null != formBean) {
       dataFactory = new ReferencesPaginator(new SpeciesBooksDomain(formBean.toSearchCriteria(), showInvalidatedSpecies));
       this.dataFactory.setSortCriteria(formBean.toSortCriteria());
     } else {
       System.out.println(TSVSpeciesReferencesReport.class.getName() + "::ctor() - Warning: formBean was null!");
     }
   }
 
   /**
    * Create the table headers.
    *
    * @return An array with the columns headers of the table
    */
   public List<String> createHeader() {
     if (null == formBean) {
       return new Vector<String>();
     }
     Vector<String> headers = new Vector<String>();
     // Author
     headers.addElement("Author");
     // Date
     headers.addElement("Date");
     // Title
     headers.addElement("Title");
     // Editor
     headers.addElement("Editor");
     // Publisher
     headers.addElement("Publisher");
     // Species
     headers.addElement("Species");
     return headers;
   }
 
   /**
    * Use this method to write specific data into the file. Implemented in inherited classes
    */
   public void writeData() {
     if (null == dataFactory) {
       return;
     }
     dataFactory.setPageSize(RESULTS_PER_PAGE);
     try {
       int _pagesCount = dataFactory.countPages();
       if (_pagesCount == 0) {
         closeFile();
         return;
       }
       writeRow(createHeader());
       xmlreport.writeRow(createHeader());
       for (int _currPage = 0; _currPage < _pagesCount; _currPage++) {
         List resultSet = dataFactory.getPage(_currPage);
         for (int i = 0; i < resultSet.size(); i++) {
           SpeciesBooksPersist book = (SpeciesBooksPersist) resultSet.get(i);
 
           // Compute species
           SpeciesBooksDomain domain = new SpeciesBooksDomain(formBean.toSearchCriteria(), showInvalidatedSpecies);
           List resultsSpecies = new ArrayList();
           try {
             resultsSpecies = domain.getSpeciesForAReference(book.getId().toString());
           }
           catch (Exception e) {
             e.printStackTrace();
           }
           String species = "";
           if (resultsSpecies != null && resultsSpecies.size() > 0) {
             for (int j = 0; j < resultsSpecies.size(); j++) {
               SpeciesBooksPersist specie = (SpeciesBooksPersist) resultsSpecies.get(j);
 
               if (j == 0) {
                 Vector<String> aRow = new Vector<String>();
                 // Author
                 aRow.addElement(Utilities.formatString(book.getName()));
                 // Date
                aRow.addElement(Utilities.formatString(book.getDate()));
                 // Title
                 aRow.addElement(Utilities.formatString(book.getTitle()));
                 // Editor
                 aRow.addElement(Utilities.formatString(book.getEditor()));
                 // Publisher
                 aRow.addElement(Utilities.formatString(book.getPublisher()));
                 // Species scientific name
                 aRow.addElement(specie.getName());
                 writeRow(aRow);
               } else {
                 Vector<String> aRow = new Vector<String>();
                 // Author
                 aRow.addElement("");
                 // Date
                 aRow.addElement("");
                 // Title
                 aRow.addElement("");
                 // Editor
                 aRow.addElement("");
                 // Publisher
                 aRow.addElement("");
                 // Species scientific name
                 aRow.addElement(specie.getName());
                 writeRow(aRow);
               }
               species += "<species>" + specie.getName() + "</species>";
             }
           } else {
             Vector<String> aRow = new Vector<String>();
             // Author
             aRow.addElement(Utilities.formatString(book.getName()));
             // Date
            aRow.addElement(Utilities.formatString(book.getDate()));
             // Title
             aRow.addElement(Utilities.formatString(book.getTitle()));
             // Editor
             aRow.addElement(Utilities.formatString(book.getEditor()));
             // Publisher
             aRow.addElement(Utilities.formatString(book.getPublisher()));
             // Species scientific name
             aRow.addElement("-");
             writeRow(aRow);
           }
 
           Vector<String> aRow = new Vector<String>();
           // Author
           aRow.addElement(Utilities.formatString(book.getName()));
           // Date
          aRow.addElement(Utilities.formatString(book.getDate()));
           // Title
           aRow.addElement(Utilities.formatString(book.getTitle()));
           // Editor
           aRow.addElement(Utilities.formatString(book.getEditor()));
           // Publisher
           aRow.addElement(Utilities.formatString(book.getPublisher()));
           // Species scientific name
           aRow.addElement(species);
           xmlreport.writeRow(aRow);
         }
       }
     } catch (CriteriaMissingException ex) {
       ex.printStackTrace();
     } catch (InitializationException iex) {
       iex.printStackTrace();
     } catch (IOException ioex) {
       ioex.printStackTrace();
     } catch (Exception ex2) {
       ex2.printStackTrace();
     } finally {
       closeFile();
     }
   }
 }
