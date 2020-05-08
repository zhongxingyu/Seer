 package org.pillarone.riskanalytics.domain.pc.reinsurance.contracts.cover;
 
 import org.pillarone.riskanalytics.core.parameterization.ComboBoxTableMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier;
 import org.pillarone.riskanalytics.domain.pc.constants.LogicArguments;
 import org.pillarone.riskanalytics.domain.pc.lob.LobMarker;
 import org.pillarone.riskanalytics.domain.pc.reserves.IReserveMarker;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 public class LineOfBusinessReservesCoverAttributeStrategy
         implements ILinesOfBusinessCoverAttributeStrategy, IReservesCoverAttributeStrategy, ICombinedCoverAttributeStrategy {
 
     private ComboBoxTableMultiDimensionalParameter lines
             = new ComboBoxTableMultiDimensionalParameter(Collections.emptyList(), Arrays.asList("Covered Segments"), LobMarker.class);
     private LogicArguments connection = LogicArguments.AND;
     private ComboBoxTableMultiDimensionalParameter reserves
             = new ComboBoxTableMultiDimensionalParameter(Collections.emptyList(), Arrays.asList("Covered Reserves"), IReserveMarker.class);
 
     public IParameterObjectClassifier getType() {
        return CoverAttributeStrategyType.LINESOFBUSINESSPERILS;
     }
 
     public Map getParameters() {
         Map<String, Object> parameters = new HashMap<String, Object>(3);
         parameters.put("connection", connection);
         parameters.put("lines", lines);
         parameters.put("reserves", reserves);
         return parameters;
     }
 
     public ComboBoxTableMultiDimensionalParameter getLines() {
         return lines;
     }
 
     public ComboBoxTableMultiDimensionalParameter getReserves() {
         return reserves;
     }
 
     public LogicArguments getConnection() {
         return connection;
     }
 }
