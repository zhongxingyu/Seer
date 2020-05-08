 package com.github.tosdan.utils.sql;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.github.tosdan.utils.stringhe.MapFormatTypeValidator;
 import com.github.tosdan.utils.stringhe.MapFormatTypeValidatorSQL;
 import com.github.tosdan.utils.stringhe.TemplateCompiler;
 import com.github.tosdan.utils.stringhe.TemplateCompilerException;
 import com.github.tosdan.utils.stringhe.TemplatePicker;
 
 /**
  * 
  * @author Daniele
  * @version 0.1.0-b2013-06-28
  */
 public class MassiveQueryCompiler
 {
 	/*************************************************************************/
 	/*******************              Campi            ***********************/
 	/*************************************************************************/
 	
 	private List<Map<String, Object>> shiftingParamsMapsList;
 	private Map<String, String> repositoryIndex;
 	private String queriesRepoFolderPath;
 	private MapFormatTypeValidator validator;
 	private String templateAsString;
 	/**
 	 * Oggetto che sia in grado di interpretare i dati forniti al fine di trovare il template contenutovi.
 	 */
 	private TemplatePicker picker;
 	
 	/*************************************************************************/
 	/*******************          Costruttori          ***********************/
 	/*************************************************************************/
 	
 	/**
 	 * 
 	 * @param templateInputStram
 	 */
 	public MassiveQueryCompiler( String templateAsString ) {
 		this( templateAsString, null );
 	}
 	
 	/**
 	 * 
 	 * @param templateInputStram
 	 * @param shiftingParamsMapsList
 	 */
 	public MassiveQueryCompiler( String templateAsString, List<Map<String, Object>> shiftingParamsMapsList ) {
 		this( null, null, shiftingParamsMapsList );
 		this.templateAsString = templateAsString;
 	}
 	
 	/**
 	 * 
 	 * @param nomiQueriesDaCompilare
 	 * @param repositoryFilesIndex
 	 * @param queriesRepoFolderFullPath
 	 */
 	public MassiveQueryCompiler(Map<String, String> repositoryFilesIndex, String queriesRepoFolderFullPath ) {
 		this( repositoryFilesIndex, queriesRepoFolderFullPath, null );
 	}
 	
 	/**
 	 * 
 	 * @param nomiQueriesDaCompilare Array di nomi/id delle queries da compilare.
 	 * @param repositoryIndex Contiene le associazioni nome-query -> file-query-template. L'associazione da un id/nome di una query al file contenente il modello/template della query associata. 
 	 * @param queriesRepoFolderPath Percorso assoluto completo della cartella (radice) contenente i template delle queries da compilare
 	 * @param shiftingParamsMapsList
 	 */
 	public MassiveQueryCompiler(Map<String, String> repositoryIndex, String queriesRepoFolderPath, List<Map<String, Object>> shiftingParamsMapsList) {
 		this.shiftingParamsMapsList = shiftingParamsMapsList;
 		this.repositoryIndex = repositoryIndex;
 		this.queriesRepoFolderPath = queriesRepoFolderPath;
 		// Istanzia l'oggetto di default per la validazione dei parametri rispetto ai valori effettivamente passati per evitare problemi sui Tipi
 		this.validator = new MapFormatTypeValidatorSQL();
 	}
 	
 	/*************************************************************************/
 	/*******************            Metodi             ***********************/
 	/*************************************************************************/
 	
 	/**
 	 *
 	 * @param nomeQueriesDaCompilare
 	 * @param paramsMap Mappa per la sostituzione dei parameti nelle queries (parametriche) da compilare
 	 * @return
 	 * @throws TemplateCompilerException 
 	 */
 	public Map<String, List<String>> getQueriesListMap(String nomeQueriesDaCompilare, Map<String, Object> paramsMap) throws TemplateCompilerException {
 		return this.getQueriesListMap( new String[] {nomeQueriesDaCompilare}, paramsMap );
 		
 	}
 	
 	/**
 	 * 
 	 * @param nomiQueriesDaCompilare
 	 * @param paramsMap Mappa per la sostituzione dei parameti nelle queries (parametriche) da compilare
 	 * @return Mappa con chiave nome/id Query e per valore una lista di queries: se 'shiftingParamsMapsList' e' nullo la lista conteiene sempre una sola query
 	 * @throws TemplateCompilerException
 	 */
 	public Map<String, List<String>> getQueriesListMap(String[] nomiQueriesDaCompilare, Map<String, Object> paramsMap) throws TemplateCompilerException {
 
 		Map<String, List<String>> queriesListMappedByName = new HashMap<String, List<String>>();
 
 		for( int i = 0 ; i < nomiQueriesDaCompilare.length ; i++ ) {
 			
 			List<String> compiledQueriesList = new ArrayList<String>();
 			if (this.shiftingParamsMapsList != null) {
 				Map<String, Object> tmpSingleShiftAndConstantParamsMap = null;
 				for( Map<String, Object> singleShiftParamsMap : this.shiftingParamsMapsList ) {
 					
 					tmpSingleShiftAndConstantParamsMap = new HashMap<String, Object>();
 					tmpSingleShiftAndConstantParamsMap.putAll( paramsMap );
 					tmpSingleShiftAndConstantParamsMap.putAll( singleShiftParamsMap ); // sovrascrive eventuali parametri con stessa chiave presenti nella mappa dei parametri costanti
 					
 					compiledQueriesList.add( this.getCompiledQuery(nomiQueriesDaCompilare[i], tmpSingleShiftAndConstantParamsMap) );	
 				}
 				
 			} else {
 				compiledQueriesList.add( this.getCompiledQuery(nomiQueriesDaCompilare[i], paramsMap) );
 				
 			}
 			
 			queriesListMappedByName.put( nomiQueriesDaCompilare[i], compiledQueriesList );
 		}
 			
 		return queriesListMappedByName;
 	}
 
 	/**
 	 * 
 	 * @param templateName
 	 * @param substitutesValuesMap
 	 * @return
 	 * @throws TemplateCompilerException
 	 */
 	private String getCompiledQuery( String templateName, Map<String, Object> substitutesValuesMap) 
 			throws TemplateCompilerException {
 		
 		if (this.repositoryIndex != null && this.queriesRepoFolderPath != null)
 			return TemplateCompiler.compile( repositoryIndex, queriesRepoFolderPath, templateName, substitutesValuesMap, picker, validator );
 		
 		else if (this.templateAsString != null) {
 			String s = TemplateCompiler.compile( templateAsString, templateName, substitutesValuesMap, picker, validator );
 //			System.out.println( s );
 			return s;
 		}
 		
 		else 
 			throw new IllegalArgumentException( "Errore " + this.getClass().getName() + ": repositoryIndex + queriesRepoFolderPath nulli o templateInputStram nullo. Deve esser forniuta una sorgente valida per recuperare il template da compilare." );
 	}
 
 	/**
 	 * 
 	 * @param validator
 	 */
 	public void setValidator( MapFormatTypeValidator validator ) {
 		this.validator = validator;
 	}
 
 	/**
 	 * 
 	 * @param picker Oggetto che sappia interpretare il template al fine di trovare la query sceltas.
 	 */
 	public void setPicker( TemplatePicker picker ) {
 		this.picker = picker;
 	}
 }
