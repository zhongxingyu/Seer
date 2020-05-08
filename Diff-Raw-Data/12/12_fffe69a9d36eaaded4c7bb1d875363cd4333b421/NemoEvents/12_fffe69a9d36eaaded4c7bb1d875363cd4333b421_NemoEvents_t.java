 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * <p>
  * Nemo events ver. 2.1
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public enum NemoEvents {
     // TODO check type for CONTEXT
     AG("#AG") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "AG";
                 Float value = getFloatValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     BF("#BF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "BTS_file";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     CInf("#CI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Converter name";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
                 key = "Converter version";
                 value = getStringValue(parameters);
                 parsedParameters.put(key, value);
                 key = "Converter file";
                 value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     CL("#CL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "CL";
                 Float value = getFloatValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     DL("#DL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Device label";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     DN("#DN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Device name";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     DS("#DS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Number of supported systems";
                 Integer value = getIntegerValue(parameters);
                 parsedParameters.put(key, value);
                 key = "Supported systems";
                 value = getIntegerValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     DT("#DT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Device type";
                 Integer value = getIntegerValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     FF("#FF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "File format version";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     EI("#EI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Device identity";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     HV("#HV") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Handler version";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     HW("#HW") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Hardware version";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     ID("#ID") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Measurement ID";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     MF("#MF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Map file";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     ML("#ML") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Measurement label";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     NN("#NN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Network name";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     PC("#PC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet capture state";
                 Integer value = getIntegerValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     PRODUCT("#PRODUCT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Product name";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
                 key = "Product version";
                 value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     SI("#SI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Subscriber identity";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     SP("#SP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Subscriber phone number";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     SW("#SW") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Device software version";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     TS("#TS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Test script file";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     UT("#UT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Gap to UTC";
                 Integer value = getIntegerValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     VQ("#VQ") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "VQ type";
                 Integer value = getIntegerValue(parameters);
                 parsedParameters.put(key, value);
                 key = "VQ version";
                 String valueStr = getStringValue(parameters);
                 parsedParameters.put(key, valueStr);
             }
             return parsedParameters;
         }
     },
     START("#START") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Date";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     STOP("#STOP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Date";
                 String value = getStringValue(parameters);
                 parsedParameters.put(key, value);
             }
             return parsedParameters;
         }
     },
     CAA("CAA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Call context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Call type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Direction";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Number";
                 parsedParameters.put(key, getStringValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 String key = "Call type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#MOC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Number";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     CAI("CAI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "Call type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#MTC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Number";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     CAC("CAC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Call context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Call type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Call status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Parameters";
                 parsedParameters.put(key, getStringValue(parameters));
                 parsedParameters.put("TN", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "Call status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Call att. time";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TN";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     CAF("CAF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Call context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Call type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CS fail. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("CS disc. cause", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "CS fail. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CS fail. time";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CS fail. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     CAD("CAD") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Call context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Call type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CS disc. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("CS disc. cause", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "CS disc. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CS call dur.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CS disc. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     VCHI("VCHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             String key = "System";
             final Integer system = getIntegerValue(parameters);
             parsedParameters.put(key, system);
             if (system == 2) {
                 List<String> contextName = new ArrayList<String>(1);
                 key = "Call context ID";
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 parsedParameters.put("PTT state", getIntegerValue(parameters));
                 parsedParameters.put("PTT comm. type", getIntegerValue(parameters));
                 parsedParameters.put("PTT user identity", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     DAA("DAA") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data connection context ID";
                 List<String> contextName = new ArrayList<String>(3);
                 contextName.add(key);
                 key = "Packet session context ID";
                 contextName.add(key);
                 key = "Call context ID";
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Host address";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Host port";
                 parsedParameters.put(key, getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 String key = "Data protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Host address";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Host port";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DAC("DAC") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data connection context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "Conn. time";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Conn. rate DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Conn. rate UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DAF("DAF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data connection context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 final Integer protocol = getIntegerValue(parameters);
                 parsedParameters.put(key, protocol);
                 key = "Data fail. status";
                 final Integer failStatus = getIntegerValue(parameters);
                 parsedParameters.put(key, failStatus);
                 if (failStatus == 2) {
                     parsedParameters.put("Socket cause", getIntegerValue(parameters));
                 } else {
                     // reserved 1 field
                     getStringValue(parameters);
                 }
                 if (protocol == 0 || protocol == 1 || protocol == 2) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 } else if (3 == protocol) {
                     parsedParameters.put("FTP cause", getIntegerValue(parameters));
 
                 } else if (4 == protocol) {
                     parsedParameters.put("HTTP cause", getIntegerValue(parameters));
                 } else if (5 == protocol) {
                     parsedParameters.put("SMTP cause", getIntegerValue(parameters));
                 } else if (6 == protocol) {
                     parsedParameters.put("POP3 cause", getIntegerValue(parameters));
                 } else if (7 == protocol || 8 == protocol || 10 == protocol) {
                     parsedParameters.put("WAP and MMS cause", getIntegerValue(parameters));
                 } else if (9 == protocol) {
                     parsedParameters.put("Streaming cause", getIntegerValue(parameters));
                 } else if (11 == protocol) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 } else if (12 == protocol) {
                     parsedParameters.put("ICMP ping cause", getIntegerValue(parameters));
                 } else if (13 == protocol || 14 == protocol) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 }
 
             } else if ("1.86".equals(version)) {
                 String key = "Data fail. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Data fail. time";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Data fail. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DAD("DAD") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data connection context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 Integer protocol = getIntegerValue(parameters);
                 parsedParameters.put(key, protocol);
                 key = "Data disc. status";
                 Integer failStatus = getIntegerValue(parameters);
                 parsedParameters.put(key, failStatus);
                 if (failStatus == 2) {
                     parsedParameters.put("Socket cause", getIntegerValue(parameters));
                 } else {
                     // reserved 1 field
                     getStringValue(parameters);
                 }
                 if (protocol == 0 || protocol == 1 || protocol == 2) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 } else if (3 == protocol) {
                     parsedParameters.put("FTP cause", getIntegerValue(parameters));
 
                 } else if (4 == protocol) {
                     parsedParameters.put("HTTP cause", getIntegerValue(parameters));
                 } else if (5 == protocol) {
                     parsedParameters.put("SMTP cause", getIntegerValue(parameters));
                 } else if (6 == protocol) {
                     parsedParameters.put("POP3 cause", getIntegerValue(parameters));
                 } else if (7 == protocol || 8 == protocol || 10 == protocol) {
                     parsedParameters.put("WAP and MMS cause", getIntegerValue(parameters));
                 } else if (9 == protocol) {
                     parsedParameters.put("Streaming cause", getIntegerValue(parameters));
                 } else if (11 == protocol) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 } else if (12 == protocol) {
                     parsedParameters.put("ICMP ping cause", getIntegerValue(parameters));
                 } else if (13 == protocol || 14 == protocol) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "Data disc. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Conn. duration";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Disc. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DREQ("DREQ") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 List<String> contextName = new ArrayList<String>(2);
                 String key = "Data transfer context ID";
                 contextName.add(key);
                 key = "Data connection context ID";
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 final Integer protocol = getIntegerValue(parameters);
                 parsedParameters.put(key, protocol);
                 key = "Transf. dir.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 if (0 == protocol || 1 == protocol || 2 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Packet size", getIntegerValue(parameters));
                     parsedParameters.put("Rate limit", getIntegerValue(parameters));
                     parsedParameters.put("Ping size", getIntegerValue(parameters));
                     parsedParameters.put("Ping rate", getIntegerValue(parameters));
                     parsedParameters.put("Ping timeout", getIntegerValue(parameters));
                 } else if (3 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Filename", getStringValue(parameters));
                     parsedParameters.put("Transf. att. #", getIntegerValue(parameters));
                 } else if (4 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Filename", getStringValue(parameters));
                     parsedParameters.put("Transf. att. #", getIntegerValue(parameters));
                 } else if (5 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Filename", getStringValue(parameters));
                 } else if (6 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Filename", getStringValue(parameters));
                 } else if (7 == protocol) {
                     parsedParameters.put("MMS file size", getIntegerValue(parameters));
                     parsedParameters.put("MMS filename", getStringValue(parameters));
                 } else if (8 == protocol || 10 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Filename", getStringValue(parameters));
                 } else if (9 == protocol) {
                     parsedParameters.put("File size", getIntegerValue(parameters));
                     parsedParameters.put("Filename", getStringValue(parameters));
                 } else if (11 == protocol) {
                     parsedParameters.put("Filename", getStringValue(parameters));
                 } else if (12 == protocol) {
                     parsedParameters.put("Ping size", getIntegerValue(parameters));
                     parsedParameters.put("Ping rate", getIntegerValue(parameters));
                     parsedParameters.put("Ping timeout", getIntegerValue(parameters));
                 } else if (13 == protocol || 14 == protocol) {
                     parsedParameters.put("Data size", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
 
                 String key = "Data protocol";
                 final Integer dataProt = getIntegerValue(parameters);
                 parsedParameters.put(key, dataProt);
                 key = "Transf. dir.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 if (dataProt == 11) {
                     key = "File name";
                     parsedParameters.put(key, getStringValue(parameters));
                 } else {
                     key = "File size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     if (dataProt < 3) {
                         key = "Packet size";
                         parsedParameters.put(key, getIntegerValue(parameters));
                         key = "Rate limit";
                         parsedParameters.put(key, getIntegerValue(parameters));
                         key = "Ping size";
                         parsedParameters.put(key, getIntegerValue(parameters));
                         key = "Ping rate";
                         parsedParameters.put(key, getIntegerValue(parameters));
                         key = "Ping timeout";
                         parsedParameters.put(key, getIntegerValue(parameters));
                     } else {
                         key = "File name";
                         parsedParameters.put(key, getStringValue(parameters));
                         if (dataProt == 3 || dataProt == 4) {
                             key = "Transf. att. #";
                             parsedParameters.put(key, getIntegerValue(parameters));
                         }
                     }
                 }
             }
             return parsedParameters;
         }
     },
     DCOMP("DCOMP") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data transfer context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 final Integer protocol = getIntegerValue(parameters);
                 parsedParameters.put(key, protocol);
                 key = "Transf. status";
                 Integer failStatus = getIntegerValue(parameters);
                 parsedParameters.put(key, failStatus);
                 if (failStatus == 2) {
                     parsedParameters.put("Socket cause", getIntegerValue(parameters));
                 } else {
                     // reserved 1 field
                     getStringValue(parameters);
                 }
                 if (protocol == 0 || protocol == 1 || protocol == 2) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 } else if (3 == protocol) {
                     parsedParameters.put("FTP cause", getIntegerValue(parameters));
 
                 } else if (4 == protocol) {
                     parsedParameters.put("HTTP cause", getIntegerValue(parameters));
                 } else if (5 == protocol) {
                     parsedParameters.put("SMTP cause", getIntegerValue(parameters));
                 } else if (6 == protocol) {
                     parsedParameters.put("POP3 cause", getIntegerValue(parameters));
                 } else if (7 == protocol || 8 == protocol || 10 == protocol) {
                     parsedParameters.put("WAP and MMS cause", getIntegerValue(parameters));
                 } else if (9 == protocol) {
                     parsedParameters.put("Streaming cause", getIntegerValue(parameters));
                 } else if (11 == protocol) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 } else if (12 == protocol) {
                     parsedParameters.put("ICMP ping cause", getIntegerValue(parameters));
                 } else if (13 == protocol || 14 == protocol) {
                     parsedParameters.put("Data transfer cause", getIntegerValue(parameters));
                 }
 
             } else if ("1.86".equals(version)) {
                 String key = "Data protocol";
                 final Integer dataProt = getIntegerValue(parameters);
                 parsedParameters.put(key, dataProt);
                 key = "Transf. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Fail. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "IP service access time";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "IP termination time";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DAS("DAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "TPut status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "App. rate UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "App. rate DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Sent bytes";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Receiv. bytes DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DRATE("DRATE") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             String key = "Data transfer context ID";
             List<String> contextName = new ArrayList<String>(1);
             contextName.add(key);
             parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
             key = "Application protocol";
             parsedParameters.put(key, getIntegerValue(parameters));
             key = "App. rate UL";
             parsedParameters.put(key, getIntegerValue(parameters));
             key = "App. rate DL";
             parsedParameters.put(key, getIntegerValue(parameters));
             key = "Bytes UL";
             parsedParameters.put(key, getIntegerValue(parameters));
             key = "Bytes DL";
             parsedParameters.put(key, getIntegerValue(parameters));
             return parsedParameters;
         }
     },
     PER("PER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data transfer context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "PER UL";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "PER DL";
                 parsedParameters.put(key, getFloatValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "PER UL";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "PER DL";
                 parsedParameters.put(key, getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     PREQ("PREQ") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "Ping protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ping host";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Ping size";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ping rate";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ping timeout";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PCOMP("PCOMP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "Ping protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ping status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ping fail. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PING("PING") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "Ping size";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ping RTT";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RTT("RTT") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data transfer context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("Ping size", getIntegerValue(parameters));
                 parsedParameters.put("Ping RTT", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     JITTER("JITTER") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data transfer context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Application protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("Jitter UL", getIntegerValue(parameters));
                 parsedParameters.put("Jitter DL", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DSS("DSS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Application protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Data transfer context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 parsedParameters.put("Stream state", getIntegerValue(parameters));
                 parsedParameters.put("Stream bandwidth", getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 String key = "Data protocol";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Stream state";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Stream bandwith";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RXL("RXL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     key = "ARFCN";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "BSIC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RxLev full";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RxLev sub";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C1";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nARFCN";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nBSIC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nRxLev";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nC1";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nC2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                 } else {
                     key = "Channel";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "DCC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RSSI";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nSystem";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nCh";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nDCC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "nRXL";
                     parsedParameters.put(key, getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     ERXL("ERXL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     key = "#Chs";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Params/ch";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "ARFCN";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "BSIC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RxLev full";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RxLev sub";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C1";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C31";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C32";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "HCS priority";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "HCS thr.";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "CI";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "LAC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RAC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Srxlev";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Hrxlev";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Rrxlev";
                     parsedParameters.put(key, getIntegerValue(parameters));
                 } else if (system == 11) {
                     key = "#Chs";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Params/ch";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "ARFCN";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "BAND";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "LAC";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "RSSI";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C1";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "C2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "CC";
                 } else if (system == 32 || system == 35) {
                     key = "#Chs";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Params/ch";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Quality";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Channel number";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "WLAN RSSI";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "WLAN SSID";
                     parsedParameters.put(key, getStringValue(parameters));
                     key = "WLAN MAC addr.";
                     parsedParameters.put(key, getStringValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     ECI0("ECI0") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     key = "#Chs";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Carrier";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Rx power";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Params/pilot";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Act. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Set";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Cand. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN_2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0_2";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh_2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Neigh. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN_3";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0_3";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh_3";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Rem. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN_4";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0_4";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh_4";
                     parsedParameters.put(key, getIntegerValue(parameters));
                 } else if (system == 27 || system == 28 || system == 29) {
                     key = "#Chs";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Packet carrier";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Rx power";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Params/pilot";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Act. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Set";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Cand. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN_2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0_2";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh_2";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Neigh. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN_3";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0_3";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh_3";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Rem. set size";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "PN_4";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Ec/I0_4";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Walsh_4";
                     parsedParameters.put(key, getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     DCONTENT("DCONTENT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Application protocol";
                 Integer apPr = getIntegerValue(parameters);
                 parsedParameters.put(key, apPr);
                 if (apPr == 8 || apPr == 10) {
                     key = "Data transfer context ID";
                     List<String> contextName = new ArrayList<String>(1);
                     contextName.add(key);
                     parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                     parsedParameters.put("Number of content elements", getIntegerValue(parameters));
                     parsedParameters.put("Number of parameters per content", getIntegerValue(parameters));
                     parsedParameters.put("Content URL", getStringValue(parameters));
                     parsedParameters.put("Content type", getIntegerValue(parameters));
                     parsedParameters.put("Content size", getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     CELLMEAS("CELLMEAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("RxLev full", getFloatValue(parameters));
                     parsedParameters.put("RxLev sub", getFloatValue(parameters));
                     parsedParameters.put("C1", getFloatValue(parameters));
                     parsedParameters.put("C2", getFloatValue(parameters));
                     parsedParameters.put("C31", getFloatValue(parameters));
                     parsedParameters.put("C32", getFloatValue(parameters));
                     parsedParameters.put("HCS priority", getIntegerValue(parameters));
                     parsedParameters.put("HCS thr.", getFloatValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("RAC", getIntegerValue(parameters));
                     parsedParameters.put("Srxlev", getFloatValue(parameters));
                 } else if (2 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("C1", getFloatValue(parameters));
                     parsedParameters.put("C2", getFloatValue(parameters));
                     parsedParameters.put("CC", getFloatValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     final Integer group = getIntegerValue(parameters);
                     // parsedParameters.put("#Chs", group);
                     final Integer groupLen = getIntegerValue(parameters);
                     // parsedParameters.put("#params/channel", groupLen);
                     String[] ch = new String[group];
                     String[] rssi = new String[group];
                     for (int i = 0; i < group; i++) {
                         ch[i] = getStringValue(parameters);
                         rssi[i] = getStringValue(parameters);
                         if (ch[i] == null) {
                             ch[i] = "";
                         }
                         if (rssi[i] == null) {
                             rssi[i] = "";
                         }
                     }
                     parsedParameters.put("Ch_arr", ch);
                     parsedParameters.put("RSSI_arr", rssi);
                     final Integer cells = getIntegerValue(parameters);
                     // parsedParameters.put("#Cells", cells);
                     final Integer cellLen = getIntegerValue(parameters);
                     // parsedParameters.put("#params/cell", cellLen);
                     for (int i = 0; i < cells; i++) {
                         String[] param = new String[cellLen];
                         for (int j = 0; j < cellLen; j++) {
                             param[j] = getStringValue(parameters);
                             if (param[j] == null) {
                                 param[j] = "";
                             }
                         }
                         parsedParameters.put("Cell_" + i, param);
                     }
                     // parsedParameters.put("Cell type", getIntegerValue(parameters));
                     // parsedParameters.put("Band", getIntegerValue(parameters));
                     // parsedParameters.put("Ch_2", getIntegerValue(parameters));
                     // parsedParameters.put("Scr.", getIntegerValue(parameters));
                     // parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     // parsedParameters.put("STTD", getIntegerValue(parameters));
                     // parsedParameters.put("RSCP", getFloatValue(parameters));
                     // parsedParameters.put("Secondary scr.", getIntegerValue(parameters));
                     // parsedParameters.put("Squal", getFloatValue(parameters));
                     // parsedParameters.put("Srxlev", getFloatValue(parameters));
                     // parsedParameters.put("Hqual", getFloatValue(parameters));
                     // parsedParameters.put("Hrxlev", getFloatValue(parameters));
                     // parsedParameters.put("Rqual", getFloatValue(parameters));
                     // parsedParameters.put("Rrxlev", getFloatValue(parameters));
                     // parsedParameters.put("OFF", getIntegerValue(parameters));
                     // parsedParameters.put("Tm", getFloatValue(parameters));
                     // parsedParameters.put("Pathloss", getFloatValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band_2", getIntegerValue(parameters));
                     parsedParameters.put("Ch_2", getIntegerValue(parameters));
                     parsedParameters.put("Cell params ID", getIntegerValue(parameters));
                     parsedParameters.put("RSCP", getFloatValue(parameters));
                     parsedParameters.put("Srxlev", getFloatValue(parameters));
                     parsedParameters.put("Hrxlev", getFloatValue(parameters));
                     parsedParameters.put("Rrxlev", getFloatValue(parameters));
                     parsedParameters.put("Pathloss", getFloatValue(parameters));
                 } else if (11 == system || 10 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RX power", getFloatValue(parameters));
                     parsedParameters.put("RX0 power", getFloatValue(parameters));
                     parsedParameters.put("RX1 power", getFloatValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Set", getIntegerValue(parameters));
                     parsedParameters.put("Band_2", getIntegerValue(parameters));
                     parsedParameters.put("Ch_2", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Ec/I0", getFloatValue(parameters));
                     parsedParameters.put("Walsh", getIntegerValue(parameters));
                     parsedParameters.put("RSCP", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RX power", getFloatValue(parameters));
                     parsedParameters.put("RX0 power", getFloatValue(parameters));
                     parsedParameters.put("RX1 power", getFloatValue(parameters));
                     parsedParameters.put("#Chs_2", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Set", getIntegerValue(parameters));
                     parsedParameters.put("Band_2", getIntegerValue(parameters));
                     parsedParameters.put("Ch_2", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Ec/I0", getFloatValue(parameters));
                     parsedParameters.put("RSCP", getFloatValue(parameters));
                 } else if (20 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Quality", getFloatValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("SSID", getStringValue(parameters));
                     parsedParameters.put("MAC addr.", getStringValue(parameters));
                     parsedParameters.put("Security", getIntegerValue(parameters));
                     parsedParameters.put("Max transfer rate", getIntegerValue(parameters));
                 } else if (21 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Quality", getFloatValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("SSID", getStringValue(parameters));
                     parsedParameters.put("MAC addr.", getStringValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("Preamble index", getIntegerValue(parameters));
                     parsedParameters.put("BS ID", getStringValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("RSSI dev", getFloatValue(parameters));
                     parsedParameters.put("CINR dev", getFloatValue(parameters));
                 } else if (51 == system || 52 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("SAT", getIntegerValue(parameters));
                     parsedParameters.put("RxLev", getFloatValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Cell type", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("SAT", getIntegerValue(parameters));
                     parsedParameters.put("RxLev", getFloatValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     ADJMEAS("ADJMEAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("C/A channel", getIntegerValue(parameters));
                     parsedParameters.put("C/A minimum", getFloatValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("C/A -1", getFloatValue(parameters));
                     parsedParameters.put("RSSI -1", getFloatValue(parameters));
                     parsedParameters.put("C/A +1", getFloatValue(parameters));
                     parsedParameters.put("RSSI +1", getFloatValue(parameters));
                     parsedParameters.put("C/A -2", getFloatValue(parameters));
                     parsedParameters.put("RSSI -2", getFloatValue(parameters));
                     parsedParameters.put("C/A +2", getFloatValue(parameters));
                     parsedParameters.put("RSSI +2", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RXQ("RXQ") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("RXQ full", getIntegerValue(parameters));
                     parsedParameters.put("RXQ sub", getIntegerValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("BER class", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 // TODO how check GSM/DAMPS????
                 String key = "RxQual full/BER class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RxQual sub/Reserved";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PRXQ("PRXQ") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("RXQ", getIntegerValue(parameters));
                     parsedParameters.put("C value", getFloatValue(parameters));
                     parsedParameters.put("SIGN_VAR", getFloatValue(parameters));
                     parsedParameters.put("#TSL results", getIntegerValue(parameters));
                     parsedParameters.put("TSL interf.", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 // TODO how check GSM/DAMPS????
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RxQual";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "C value";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "SIGN_VAR";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "#TSL results";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TSL interf.";
                 parsedParameters.put(key, getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     FER("FER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("FER full", getFloatValue(parameters));
                     parsedParameters.put("FER sub", getFloatValue(parameters));
                     parsedParameters.put("FER TCH", getFloatValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("FER", getFloatValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("FER", getFloatValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("FER (dec)", getFloatValue(parameters));
                     parsedParameters.put("FER F-FCH target", getFloatValue(parameters));
                     parsedParameters.put("FER F-SCH target", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "FER full";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "FER sub";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "FER TCH";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     EFER("EFER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "FER (dec)";
                 parsedParameters.put(key, getFloatValue(parameters));
                 if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     key = "FER F-FCH";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "FER F-SCH";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "FER target F-FCH";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "FER target F-SCH";
                     parsedParameters.put(key, getFloatValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     MSP("MSP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("MSP", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "MSP";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RLT("RLT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("RLT", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "RLT";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     TAD("TAD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("TA", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("TA_f", getFloatValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("TAL", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "TA";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     TAL("TAL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "TAL";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DSC("DSC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("DSC current", getIntegerValue(parameters));
                 parsedParameters.put("DSC max", getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 String key = "DSC current";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "DSC max";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     BEP("BEP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("GMSK MEAN_BEP", getIntegerValue(parameters));
                 parsedParameters.put("GMSK CV_BEP", getIntegerValue(parameters));
                 parsedParameters.put("8-PSK MEAN_BEP", getIntegerValue(parameters));
                 parsedParameters.put("8-PSK CV_BEP", getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "GMSK MEAN_BEP %";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "GMSK CV_BEP %";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "8-PSK MEAN_BEP %";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "8-PSK CV_BEP %";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "GMSK MEAN_BEP";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "GMSK CV_BEP";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "8-PSK MEAN_BEP";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "8-PSK CV_BEP";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     CI("CI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("C/I", getFloatValue(parameters));
                     parsedParameters.put("#TSL results", getIntegerValue(parameters));
                     parsedParameters.put("Timeslot C/I", getFloatValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("C/I", getFloatValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("C/I", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Act set PNs", getIntegerValue(parameters));
                     parsedParameters.put("Timeslot C/I", getIntegerValue(parameters));
                     parsedParameters.put("Params/pilot", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("SINR", getFloatValue(parameters));
                     parsedParameters.put("MAC Index", getIntegerValue(parameters));
                     parsedParameters.put("DRC cover", getIntegerValue(parameters));
                     parsedParameters.put("RPC cell index", getIntegerValue(parameters));
                     parsedParameters.put("DRC cover", getIntegerValue(parameters));
                     parsedParameters.put("DRC Lock", getIntegerValue(parameters));
                     parsedParameters.put("RAB", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ch C/I";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "#TSL C/I values";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TSL C/I";
                 parsedParameters.put(key, getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     TXPC("TXPC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (2 == system) {
                     parsedParameters.put("TX power", getFloatValue(parameters));
                     parsedParameters.put("Pwr ctrl alg.", getIntegerValue(parameters));
                     parsedParameters.put("TX power change", getFloatValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("TX power", getFloatValue(parameters));
                     parsedParameters.put("Pwr ctrl alg.", getIntegerValue(parameters));
                     parsedParameters.put("Pwr ctrl step", getFloatValue(parameters));
                     parsedParameters.put("Compr. mode", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("UL pwr up %", getFloatValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("TX power", getFloatValue(parameters));
                     parsedParameters.put("Pwr ctrl step", getFloatValue(parameters));
                     parsedParameters.put("#UL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("UL pwr up %", getFloatValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("TX power", getFloatValue(parameters));
                     parsedParameters.put("Pwr ctrl step_i", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("UL pwr up %", getFloatValue(parameters));
                     parsedParameters.put("TX adjust", getFloatValue(parameters));
                     parsedParameters.put("TX pwr limit", getFloatValue(parameters));
                     parsedParameters.put("Max Power Limited", getIntegerValue(parameters));
                     parsedParameters.put("R-FCH/R-PICH", getFloatValue(parameters));
                     parsedParameters.put("R-SCH0/R-PICH", getFloatValue(parameters));
                     parsedParameters.put("R-SCH1/R-PICH", getFloatValue(parameters));
                     parsedParameters.put("R-DCCH/R-PICH", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("TX power", getFloatValue(parameters));
                     parsedParameters.put("#UL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr hold", getIntegerValue(parameters));
                     parsedParameters.put("#UL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("UL pwr up %", getFloatValue(parameters));
                     parsedParameters.put("TX adjust", getFloatValue(parameters));
                     parsedParameters.put("TX Pilot", getFloatValue(parameters));
                     parsedParameters.put("TX open loop power", getFloatValue(parameters));
                     parsedParameters.put("DRC/Pilot", getFloatValue(parameters));
                     parsedParameters.put("ACK/Pilot", getFloatValue(parameters));
                     parsedParameters.put("Data/Pilot", getFloatValue(parameters));
                     parsedParameters.put("PA Max.", getFloatValue(parameters));
                     parsedParameters.put("DRC lock period", getIntegerValue(parameters));
                     parsedParameters.put("TX throttle", getFloatValue(parameters));
                     parsedParameters.put("TX max power usage", getFloatValue(parameters));
                     parsedParameters.put("TX min power usage", getFloatValue(parameters));
                     parsedParameters.put("Transmission mode", getIntegerValue(parameters));
                     parsedParameters.put("Physical layer packet size.", getIntegerValue(parameters));
                     parsedParameters.put("RRI/Pilot", getFloatValue(parameters));
                     parsedParameters.put("DSC/Pilot", getFloatValue(parameters));
                     parsedParameters.put("AUX/Data", getFloatValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("TX power", getFloatValue(parameters));
                     parsedParameters.put("TX ref. power", getFloatValue(parameters));
                     parsedParameters.put("TX power headroom", getFloatValue(parameters));
                     parsedParameters.put("TX power BS offset", getFloatValue(parameters));
                     parsedParameters.put("TX power IrMax", getFloatValue(parameters));
                     parsedParameters.put("BS EIRP", getFloatValue(parameters));
                     parsedParameters.put("BS N+I", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     key = "TX power_i";// maybe this type Float?
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Pwr ctrl alg.";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Pwr ctrl step";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Compr. mode";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#UL pwr up";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#UL pwr down";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "UL pwr up %";
                     parsedParameters.put(key, getFloatValue(parameters));
                 } else if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 27
                         || system == 28 || system == 29 || system == 30) {
                     key = "TX power_f";// because the type of this property is different from TX
                     // power
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Pwr ctrl step";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Pwr ctrl step";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#UL pwr up";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#UL pwr down";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "UL pwr up %";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "TX adjust";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "TX pwr limit";
                     parsedParameters.put(key, getFloatValue(parameters));
                 } else if (system == 11) {
                     key = "TX power_f";// because the type of this property is different from TX
                     // power
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "Pwr ctrl alg.";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "TX pwr. change";
                     parsedParameters.put(key, getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RXPC("RXPC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("SIR target", getFloatValue(parameters));
                     parsedParameters.put("SIR", getFloatValue(parameters));
                     parsedParameters.put("BS div. state", getIntegerValue(parameters));
                     parsedParameters.put("#DL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#DL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("DL pwr up %", getFloatValue(parameters));
                     parsedParameters.put("DPC mode", getFloatValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("SIR target", getFloatValue(parameters));
                     parsedParameters.put("SIR", getFloatValue(parameters));
                     parsedParameters.put("#DL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#DL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("DL pwr up %", getFloatValue(parameters));
                     parsedParameters.put("#Timeslots", getIntegerValue(parameters));
                     parsedParameters.put("#params/timeslot", getIntegerValue(parameters));
                     parsedParameters.put("TSL", getIntegerValue(parameters));
                     parsedParameters.put("ISCP", getFloatValue(parameters));
                     parsedParameters.put("RSCP", getFloatValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("FPC mode", getIntegerValue(parameters));
                     parsedParameters.put("FPC subchannel", getIntegerValue(parameters));
                     parsedParameters.put("FPC gain", getFloatValue(parameters));
                     parsedParameters.put("#DL pwr up", getIntegerValue(parameters));
                     parsedParameters.put("#DL pwr down", getIntegerValue(parameters));
                     parsedParameters.put("DL pwr up %", getFloatValue(parameters));
                     parsedParameters.put("F-FCH cur. sp", getFloatValue(parameters));
                     parsedParameters.put("F-FCH min. sp", getFloatValue(parameters));
                     parsedParameters.put("F-FCH max. sp", getFloatValue(parameters));
                     parsedParameters.put("F-SCH0 cur. sp", getFloatValue(parameters));
                     parsedParameters.put("F-SCH0 min. sp", getFloatValue(parameters));
                     parsedParameters.put("F-SCH0 max. sp", getFloatValue(parameters));
                     parsedParameters.put("F-SCH1 cur. sp", getFloatValue(parameters));
                     parsedParameters.put("F-SCH1 min. sp", getFloatValue(parameters));
                     parsedParameters.put("F-SCH1 max. sp", getFloatValue(parameters));
                     parsedParameters.put("F-DCCH cur. sp", getFloatValue(parameters));
                     parsedParameters.put("F-DCCH min. sp", getFloatValue(parameters));
                     parsedParameters.put("F-DCCH max. sp", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     // reserved 2 fields
                     getStringValue(parameters);
                     getStringValue(parameters);
                     key = "SIR target";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "SIR";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "BS div. state";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#DL pwr up";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#DL pwr down";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "DL pwr up %";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "DPC mode";
                     parsedParameters.put(key, getIntegerValue(parameters));
 
                 } else if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     key = "FPC mode";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "FPC subch. ind.";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "FPC subch. gain.";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "#DL pwr up";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#DL pwr down";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "DL pwr up %";
                     parsedParameters.put(key, getFloatValue(parameters));
                 } else if (system == 27 || system == 28 || system == 29) {
                     key = "#Header params";// because the type of this property is different from TX
                     // power
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "DRC index";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#Act set PNs";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Params/pilot";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Pn";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "SINR";
                     parsedParameters.put(key, getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     BER("BER") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (2 == system) {
                     parsedParameters.put("BER", getFloatValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("Pilot BER", getFloatValue(parameters));
                     parsedParameters.put("TFCI BER", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     key = "Pilot BER";
                     parsedParameters.put(key, getFloatValue(parameters));
                     key = "TFCI BER";
                     parsedParameters.put(key, getFloatValue(parameters));
                 } else if (system == 11) {
                     key = "BER";
                     parsedParameters.put(key, getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     ECNO("ECNO") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "#Chs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Channel";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Carrier RSSI";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#params/cell";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Active cells";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Channel_1";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Scrambling code";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ec/No";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "STTD";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RSSP";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Secondary scr.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Squal";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Srxlev";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hqual";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hrxlev";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rqual";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rrxlev";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "OFF";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TM";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "#Monit. cells";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Channel_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Scrambling code_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ec/No_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "STTD_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RSSP_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Secondary scr._2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Squal_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Srxlev_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hqual_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hrxlev_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rqual_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rrxlev_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "OFF_2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TM_2";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "#Detect. cells";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Channel_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Scrambling code_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ec/No_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "STTD_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RSSP_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Secondary scr._3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Squal_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Srxlev_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hqual_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hrxlev_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rqual_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rrxlev_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "OFF_3";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TM_3";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "#Undet. cells";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Channel_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Scrambling code_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ec/No_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "STTD_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RSSP_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Secondary scr._4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Squal_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Srxlev_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hqual_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Hrxlev_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rqual_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Rrxlev_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "OFF_4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TM_4";
                 parsedParameters.put(key, getFloatValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     PHDAS("PHDAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "DPDCH rate UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PPPDAS("PPPDAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "PPP rate UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "PPP rate DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Sent PPP bytes";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Recv. PPP bytes";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     WLANDAS("WLANDAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (system == 32) {
                     key = "WLAN rate UL";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "WLAN rate DL";
                     parsedParameters.put(key, getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RLPDAS("RLPDAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "System";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "RLP R-rate";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RLP F-rate";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RLP R-retr";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "RLP F-retr";
                 parsedParameters.put(key, getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     PHRATE("PHRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("DPDCH rate UL", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     WLANRATE("WLANRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "WLAN rate UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "WLAN rate DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PPPRATE("PPPRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "PPP rate UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "PPP rate DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Sent PPP bytes";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Recv. PPP bytes";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RLPRATE("RLPRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("RLP rev. rate", getIntegerValue(parameters));
                 parsedParameters.put("RLP for. rate", getIntegerValue(parameters));
                 parsedParameters.put("RLP rev. retr. rate", getFloatValue(parameters));
                 parsedParameters.put("RLP fwd. retr. rate", getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     RLPSTATISTICS("RLPSTATISTICS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (10 == system || 11 == system) {
                     parsedParameters.put("Service ID", getIntegerValue(parameters));
                     parsedParameters.put("Resets", getIntegerValue(parameters));
                     parsedParameters.put("Aborts", getIntegerValue(parameters));
                     parsedParameters.put("Last RTT", getIntegerValue(parameters));
                     parsedParameters.put("Block of bytes used", getIntegerValue(parameters));
                     parsedParameters.put("RX NAKs", getIntegerValue(parameters));
                     parsedParameters.put("Largest Con. Erasures", getIntegerValue(parameters));
                     parsedParameters.put("Retrans. not found", getIntegerValue(parameters));
                     parsedParameters.put("RX retrans. frames", getIntegerValue(parameters));
                     parsedParameters.put("RX idle frames", getIntegerValue(parameters));
                     parsedParameters.put("RX fill frames", getIntegerValue(parameters));
                     parsedParameters.put("RX blank frames", getIntegerValue(parameters));
                     parsedParameters.put("RX null frames", getIntegerValue(parameters));
                     parsedParameters.put("RX new frames", getIntegerValue(parameters));
                     parsedParameters.put("RX fund. frames", getIntegerValue(parameters));
                     parsedParameters.put("RX bytes", getIntegerValue(parameters));
                     parsedParameters.put("RX RLP erasures", getIntegerValue(parameters));
                     parsedParameters.put("RX MUX erasures", getIntegerValue(parameters));
                     parsedParameters.put("TX NAKs", getIntegerValue(parameters));
                     parsedParameters.put("TX retrans. frames", getIntegerValue(parameters));
                     parsedParameters.put("TX idle frames", getIntegerValue(parameters));
                     parsedParameters.put("TX new frames", getIntegerValue(parameters));
                     parsedParameters.put("TX fund. frames", getIntegerValue(parameters));
                     parsedParameters.put("TX bytes", getIntegerValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("Service ID", getIntegerValue(parameters));
                     parsedParameters.put("RX NAKs", getIntegerValue(parameters));
                     parsedParameters.put("RX NAKs in bytes", getIntegerValue(parameters));
                     parsedParameters.put("Retrans. not found", getIntegerValue(parameters));
                     parsedParameters.put("RX dup. bytes", getIntegerValue(parameters));
                     parsedParameters.put("RX retrans. bytes", getIntegerValue(parameters));
                     parsedParameters.put("RX new bytes", getIntegerValue(parameters));
                     parsedParameters.put("RX bytes", getIntegerValue(parameters));
                     parsedParameters.put("RX NAKs", getIntegerValue(parameters));
                     parsedParameters.put("TX NAKs in bytes", getIntegerValue(parameters));
                     parsedParameters.put("TX retrans. bytes", getIntegerValue(parameters));
                     parsedParameters.put("TX new bytes", getIntegerValue(parameters));
                     parsedParameters.put("TX bytes", getIntegerValue(parameters));
                     parsedParameters.put("NAK timeouts", getIntegerValue(parameters));
                     parsedParameters.put("Reset count", getIntegerValue(parameters));
                     parsedParameters.put("AT reset request count", getIntegerValue(parameters));
                     parsedParameters.put("AN reset ack count", getIntegerValue(parameters));
                     parsedParameters.put("AN reset request count", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     MEI("MEI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("Measurement event", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Event_m";// because "event" property maybe exist
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     CQI("CQI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Params", getIntegerValue(parameters));
                     parsedParameters.put("Sample dur.", getIntegerValue(parameters));
                     parsedParameters.put("Ph req. Rate", getIntegerValue(parameters));
                     parsedParameters.put("CQI repetitions", getIntegerValue(parameters));
                     parsedParameters.put("CQI cycle", getIntegerValue(parameters));
                     parsedParameters.put("#CQI values", getIntegerValue(parameters));
                     parsedParameters.put("#Params per CQI", getIntegerValue(parameters));
                     parsedParameters.put("Percentage", getFloatValue(parameters));
                     parsedParameters.put("CQI", getIntegerValue(parameters));
                 }
 
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#CQI header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Sample dur.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ph. req. TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "CQI repetitions";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#CQI cycle";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#CQI values";
                 final Integer cycle = getIntegerValue(parameters);
                 parsedParameters.put(key, cycle);
                 key = "#Params/CQI";
                 final Integer cycleLen = getIntegerValue(parameters);
                 parsedParameters.put(key, cycleLen);
                 if (cycle != null || cycleLen != null) {
                     for (int i = 0; i < cycle; i++) {
                         List<String> param = new ArrayList<String>();
                         for (int j = 0; j < cycleLen; j++) {
                             param.add(getStringValue(parameters));
                         }
                         parsedParameters.put("CQI_" + i, param.toArray(new String[0]));
                     }
                 }
 
             }
             return parsedParameters;
         }
     },
     HARQI("HARQI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#HARQ processes", getIntegerValue(parameters));
                     parsedParameters.put("#Params/HARQ", getIntegerValue(parameters));
                     parsedParameters.put("HARQ ID", getIntegerValue(parameters));
                     parsedParameters.put("HARQ dir.", getIntegerValue(parameters));
                     parsedParameters.put("HARQ Rate", getIntegerValue(parameters));
                     parsedParameters.put("#HARQ packets", getIntegerValue(parameters));
                     parsedParameters.put("HARQ BLER", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#HARQ processes";
                 final Integer cycle = getIntegerValue(parameters);
                 parsedParameters.put(key, cycle);
                 key = "#Params/HARQ";
                 final Integer cycleLen = getIntegerValue(parameters);
                 parsedParameters.put(key, cycleLen);
                 if (cycle != null || cycleLen != null) {
                     for (int i = 0; i < cycle; i++) {
                         List<String> param = new ArrayList<String>();
                         for (int j = 0; j < cycleLen; j++) {
                             param.add(getStringValue(parameters));
                         }
                         parsedParameters.put("HARQ_" + i, param.toArray(new String[0]));
                     }
                 }
 
             }
             return parsedParameters;
         }
     },
     HSSCCHI("HSSCCHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("HS-SCCH code", getIntegerValue(parameters));
                     parsedParameters.put("HSDPA HS-SCCH usage", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#HS-SCCH channels";
                 final Integer cycle = getIntegerValue(parameters);
                 parsedParameters.put(key, cycle);
                 key = "#Params/HS-SCCH";
                 final Integer cycleLen = getIntegerValue(parameters);
                 parsedParameters.put(key, cycleLen);
                 if (cycle != null || cycleLen != null) {
                     for (int i = 0; i < cycle; i++) {
                         List<String> param = new ArrayList<String>();
                         for (int j = 0; j < cycleLen; j++) {
                             param.add(getStringValue(parameters));
                         }
                         parsedParameters.put("HSSCCHI_" + i, param.toArray(new String[0]));
                     }
                 }
 
             }
             return parsedParameters;
         }
     },
     PLAI("PLAI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "Packet technology";
                 final Integer pt = getIntegerValue(parameters);
                 parsedParameters.put(key, pt);
                 if (5 == pt) {
                     key = "#PLA header parameters";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "Sample dur.";
                     parsedParameters.put(key, getIntegerValue(parameters));
                     key = "#PLA sets";
                     final Integer cycle = getIntegerValue(parameters);
                     parsedParameters.put(key, cycle);
                     key = "#params/PLA set";
                     final Integer cycleLen = getIntegerValue(parameters);
                     parsedParameters.put(key, cycleLen);
                     if (cycle != null || cycleLen != null) {
                         for (int i = 0; i < cycle; i++) {
                             List<String> param = new ArrayList<String>();
                             for (int j = 0; j < cycleLen; j++) {
                                 param.add(getStringValue(parameters));
                             }
                             parsedParameters.put("PLAI_" + i, param.toArray(new String[0]));
                         }
                     }
                 }
             }
             return parsedParameters;
         }
     },
     PLAID("PLAID") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Sample duration", getIntegerValue(parameters));
                     parsedParameters.put("#PLA sets", getIntegerValue(parameters));
                     parsedParameters.put("HS-PDSCH Rate", getIntegerValue(parameters));
                     parsedParameters.put("#params/PLA set", getIntegerValue(parameters));
                     parsedParameters.put("Percentage", getFloatValue(parameters));
                     parsedParameters.put("Modulation", getIntegerValue(parameters));
                     parsedParameters.put("Effective coding", getFloatValue(parameters));
                     parsedParameters.put("TB size", getIntegerValue(parameters));
                     parsedParameters.put("1st ch. code", getIntegerValue(parameters));
                     parsedParameters.put("#codes", getIntegerValue(parameters));
                     parsedParameters.put("Retr.", getFloatValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("#PLA header parameters", getIntegerValue(parameters));
                     parsedParameters.put("Sample duration", getIntegerValue(parameters));
                     parsedParameters.put("Burst count", getIntegerValue(parameters));
                     parsedParameters.put("#PLA sets", getIntegerValue(parameters));
                     parsedParameters.put("#params/PLA set", getIntegerValue(parameters));
                     parsedParameters.put("Percentage", getFloatValue(parameters));
                     parsedParameters.put("Modulation", getIntegerValue(parameters));
                     parsedParameters.put("Coding rate", getIntegerValue(parameters));
                     parsedParameters.put("Coding type", getIntegerValue(parameters));
                     parsedParameters.put("Repetition coding", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     PLAIU("PLAIU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Sample duration", getIntegerValue(parameters));
                     parsedParameters.put("E-DPDCH Rate", getIntegerValue(parameters));
                     parsedParameters.put("Lim. max power", getFloatValue(parameters));
                     parsedParameters.put("Lim. grant", getFloatValue(parameters));
                     parsedParameters.put("Lim. lack of data", getFloatValue(parameters));
                     parsedParameters.put("Lim. by mux", getFloatValue(parameters));
                     parsedParameters.put("Lim. by HARQ", getFloatValue(parameters));
                     parsedParameters.put("#PLA sets", getIntegerValue(parameters));
                     parsedParameters.put("#params/PLA set", getIntegerValue(parameters));
                     parsedParameters.put("Percentage", getFloatValue(parameters));
                     parsedParameters.put("Modulation", getIntegerValue(parameters));
                     parsedParameters.put("TB size", getIntegerValue(parameters));
                     parsedParameters.put("E-TFCI", getIntegerValue(parameters));
                     parsedParameters.put("SFs", getIntegerValue(parameters));
                     parsedParameters.put("Retr.", getFloatValue(parameters));
                     parsedParameters.put("Avg. SG index", getIntegerValue(parameters));
                     parsedParameters.put("Avg. SG", getFloatValue(parameters));
 
                 } else if (25 == system) {
                     parsedParameters.put("#PLA header parameters", getIntegerValue(parameters));
                     parsedParameters.put("Sample duration", getIntegerValue(parameters));
                     parsedParameters.put("Burst count", getIntegerValue(parameters));
                     parsedParameters.put("#PLA sets", getIntegerValue(parameters));
                     parsedParameters.put("#params/PLA set", getIntegerValue(parameters));
                     parsedParameters.put("Percentage", getFloatValue(parameters));
                     parsedParameters.put("Modulation", getIntegerValue(parameters));
                     parsedParameters.put("Coding rate", getIntegerValue(parameters));
                     parsedParameters.put("Coding type", getIntegerValue(parameters));
                     parsedParameters.put("Repetition coding", getIntegerValue(parameters));
 
                 }
             }
             return parsedParameters;
         }
     },
     HBI("HBI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("Reporting interval", getIntegerValue(parameters));
                     parsedParameters.put("Happy bit", getFloatValue(parameters));
                     parsedParameters.put("DTX", getFloatValue(parameters));
 
                 }
             }
             return parsedParameters;
         }
     },
     MACERATE("MACERATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("MAC-e bitrate", getIntegerValue(parameters));
                     parsedParameters.put("MAC-e block rate", getIntegerValue(parameters));
                     parsedParameters.put("MAC-e 1st retr.", getFloatValue(parameters));
                     parsedParameters.put("MAC-e 2nd retr.", getFloatValue(parameters));
                     parsedParameters.put("MAC-e 3rd retr.", getFloatValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     AGRANT("AGRANT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("AGCH index", getIntegerValue(parameters));
                     parsedParameters.put("AGCH grant", getFloatValue(parameters));
                     parsedParameters.put("AGCH scope", getIntegerValue(parameters));
                     parsedParameters.put("AGCH selector", getIntegerValue(parameters));
                     parsedParameters.put("E-RNTI selector", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SGRANT("SGRANT") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Sample dur.", getIntegerValue(parameters));
                     parsedParameters.put("#SG sets", getIntegerValue(parameters));
                     parsedParameters.put("#params/SG set", getIntegerValue(parameters));
                     parsedParameters.put("Distribution", getFloatValue(parameters));
                     parsedParameters.put("SG index", getIntegerValue(parameters));
                     parsedParameters.put("Serving grant", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     EDCHI("EDCHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("NS ACKs %", getFloatValue(parameters));
                     parsedParameters.put("NS grant down %", getFloatValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getFloatValue(parameters));
                     parsedParameters.put("HSUPA channel", getIntegerValue(parameters));
                     parsedParameters.put("HSUPA SC", getIntegerValue(parameters));
                     parsedParameters.put("HSUPA RLS", getIntegerValue(parameters));
                     parsedParameters.put("ACK %", getFloatValue(parameters));
                     parsedParameters.put("NACK %", getFloatValue(parameters));
                     parsedParameters.put("DTX %", getFloatValue(parameters));
                     parsedParameters.put("Grant up %", getFloatValue(parameters));
                     parsedParameters.put("Grant hold %", getFloatValue(parameters));
                     parsedParameters.put("Grant down %", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     HSUPASI("HSUPASI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("Dur.", getIntegerValue(parameters));
                     parsedParameters.put("SI count", getIntegerValue(parameters));
                     parsedParameters.put("HLID", getIntegerValue(parameters));
                     parsedParameters.put("HLBS", getIntegerValue(parameters));
                     parsedParameters.put("TEBS", getIntegerValue(parameters));
                     parsedParameters.put("TEBS min", getIntegerValue(parameters));
                     parsedParameters.put("TEBS max", getIntegerValue(parameters));
                     parsedParameters.put("UPH", getIntegerValue(parameters));
                     parsedParameters.put("UPH min", getIntegerValue(parameters));
                     parsedParameters.put("UPH max", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     DRCI("DRCI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Sample duration";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#DRC sets";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#params/DRC set";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Percentage";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "Requested rate";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Packet length";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
 
         }
     },
     RDRC("RDRC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("TX rate limit", getIntegerValue(parameters));
                     parsedParameters.put("TX current rate", getIntegerValue(parameters));
                     parsedParameters.put("Comb. RAB", getIntegerValue(parameters));
                     parsedParameters.put("PA max", getIntegerValue(parameters));
                     parsedParameters.put("Random variable", getIntegerValue(parameters));
                     parsedParameters.put("Transition probability", getIntegerValue(parameters));
                     parsedParameters.put("Condition RRI", getIntegerValue(parameters));
                     parsedParameters.put("Actual RRI", getIntegerValue(parameters));
                     parsedParameters.put("Padding bytes", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     FDRC("FDRC") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("DRC index", getIntegerValue(parameters));
                     parsedParameters.put("DRC cover", getIntegerValue(parameters));
                     parsedParameters.put("DSC value", getIntegerValue(parameters));
                     parsedParameters.put("DRC boost", getIntegerValue(parameters));
                     parsedParameters.put("DSC boost", getIntegerValue(parameters));
                     parsedParameters.put("DRC lock upd. slot", getIntegerValue(parameters));
                     parsedParameters.put("ACK channel status", getIntegerValue(parameters));
                     parsedParameters.put("Forced ACK/NAK ratio", getFloatValue(parameters));
                     parsedParameters.put("ACK ratio", getFloatValue(parameters));
                     parsedParameters.put("Multiuser ACK ratio", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     PHFER("PHFER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("PER inst.", getFloatValue(parameters));
                     parsedParameters.put("PER short", getFloatValue(parameters));
                     parsedParameters.put("PER long", getFloatValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("FER", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     MARKOVMUX("MARKOVMUX") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (11 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Frames", getIntegerValue(parameters));
                     parsedParameters.put("Params/frame", getIntegerValue(parameters));
                     parsedParameters.put("M expecteted mux", getIntegerValue(parameters));
                     parsedParameters.put("M actual mux", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     MARKOVSTATS("MARKOVSTATS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "M FER";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "#expected values";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "M expected";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "M 1/1";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "M 1/2";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "M 1/4";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "M 1/8";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "M erasures";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
 
         }
     },
     MER("MER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (2 == system) {
                     parsedParameters.put("MER", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "MER";
                 parsedParameters.put(key, getFloatValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     DVBI("DVBI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (65 == system) {
                     parsedParameters.put("Service state", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("Bandwidth", getFloatValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                     parsedParameters.put("Tx mode", getIntegerValue(parameters));
                     parsedParameters.put("Modulation", getIntegerValue(parameters));
                     parsedParameters.put("Code rate LP", getIntegerValue(parameters));
                     parsedParameters.put("Code rate HP", getIntegerValue(parameters));
                     parsedParameters.put("Guard time", getIntegerValue(parameters));
                     parsedParameters.put("MPE-FEC code rate LP", getIntegerValue(parameters));
                     parsedParameters.put("MPE-FEC code rate HP", getIntegerValue(parameters));
                     parsedParameters.put("Hierarchy", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 Integer dvbType = getIntegerValue(parameters);
                 parsedParameters.put("DVB type", dvbType);
                 if (dvbType == 1) {
                     parsedParameters.put("Service state", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("Bandwidth", getFloatValue(parameters));
                     parsedParameters.put("Cell identifier", getIntegerValue(parameters));
                     parsedParameters.put("Transmission mode", getIntegerValue(parameters));
                     parsedParameters.put("Modulation", getIntegerValue(parameters));
                     parsedParameters.put("Code rate LP", getIntegerValue(parameters));
                     parsedParameters.put("Code rate HP", getIntegerValue(parameters));
                     parsedParameters.put("Guard time", getIntegerValue(parameters));
                     parsedParameters.put("MPE-FEC code rate LP", getIntegerValue(parameters));
                     parsedParameters.put("MPE-FEC code rate HP", getIntegerValue(parameters));
                     parsedParameters.put("Hierarchy", getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     DVBFER("DVBFER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (65 == system) {
                     parsedParameters.put("FER", getFloatValue(parameters));
                     parsedParameters.put("MFER", getFloatValue(parameters));
                     parsedParameters.put("Frame count", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 Integer dvbType = getIntegerValue(parameters);
                 parsedParameters.put("DVB type", dvbType);
                 if (dvbType == 1) {
                     parsedParameters.put("FER", getFloatValue(parameters));
                     parsedParameters.put("MFER", getFloatValue(parameters));
                     parsedParameters.put("Frame count", getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     DVBBER("DVBBER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (65 == system) {
                     parsedParameters.put("BER", getFloatValue(parameters));
                     parsedParameters.put("VBER", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 Integer dvbType = getIntegerValue(parameters);
                 parsedParameters.put("DVB type", dvbType);
                 if (dvbType == 1) {
                     parsedParameters.put("BER", getFloatValue(parameters));
                     parsedParameters.put("VBER", getFloatValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     DVBRXL("DVBRXL") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (65 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("RxLev", getFloatValue(parameters));
                     parsedParameters.put("C/N", getFloatValue(parameters));
                     parsedParameters.put("Signal quality", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 Integer dvbType = getIntegerValue(parameters);
                 parsedParameters.put("DVB type", dvbType);
                 if (dvbType == 1) {
                     parsedParameters.put("Num. header params", getIntegerValue(parameters));
                     final Integer cycle = getIntegerValue(parameters);
                     parsedParameters.put("#Channel", cycle);
                     final Integer cycleLen = getIntegerValue(parameters);
                     parsedParameters.put("#Params per ch.", cycleLen);
                     if (cycle != null || cycleLen != null) {
                         for (int i = 0; i < cycle; i++) {
                             List<String> param = new ArrayList<String>();
                             for (int j = 0; j < cycleLen; j++) {
                                 param.add(getStringValue(parameters));
                             }
                             parsedParameters.put("DVBRXL_" + i, param.toArray(new String[0]));
                         }
                     }
                 }
 
             }
             return parsedParameters;
         }
     },
     SCAN("SCAN") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("RxLev", getFloatValue(parameters));
                 } else if (system == 4 || system == 5 || system == 6 || system == 9 || system == 10 || system == 23 || system == 24
                         || system == 25) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("DCC", getIntegerValue(parameters));
                     parsedParameters.put("RXL", getFloatValue(parameters));
                 } else if (system == 7 || system == 8 || system == 26) {
                     final Integer carrier = getIntegerValue(parameters);
                     if (carrier != 0) {
                         parsedParameters.put("Carrier", carrier);
                         parsedParameters.put("PN", getIntegerValue(parameters));
                         parsedParameters.put("Ec/Io", getFloatValue(parameters));
                     } else {
                         // reserved 1 field
                         getStringValue(parameters);
                         parsedParameters.put("Carrier", getIntegerValue(parameters));
                         parsedParameters.put("RSSI", getFloatValue(parameters));
                     }
                 } else if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33
                         || system == 34) {
                     final Integer channel = getIntegerValue(parameters);
                     if (channel != 0) {
                         parsedParameters.put("Channel", channel);
                         parsedParameters.put("RSSI", getFloatValue(parameters));
                         parsedParameters.put("Ch type", getIntegerValue(parameters));
                         parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                         parsedParameters.put("Ec/No", getFloatValue(parameters));
                     } else {
                         parsedParameters.put("Channel number", channel);
                         // 2 field not used
                         getStringValue(parameters);
                         getStringValue(parameters);
                         parsedParameters.put("Channel", getIntegerValue(parameters));
                         parsedParameters.put("RSSI", getFloatValue(parameters));
                     }
                 }
             }
             return parsedParameters;
         }
     },
     TSCAN("TSCAN") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Chip", getIntegerValue(parameters));
                     parsedParameters.put("Ec/No", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     DSCAN("DSCAN") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                     parsedParameters.put("Delay spread", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     DELAY("DELAY") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 7 || system == 8 || system == 26) {
                     parsedParameters.put("Carrier", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Delay", getFloatValue(parameters));
 
                 } else if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33
                         || system == 34) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                     parsedParameters.put("Delay spread", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RSCP("RSCP") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("Channel", getIntegerValue(parameters));
                 parsedParameters.put("RSSI", getFloatValue(parameters));
                 parsedParameters.put("Ch type", getIntegerValue(parameters));
                 parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                 parsedParameters.put("RSCP", getFloatValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     SSCAN("SSCAN") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("Channel", getIntegerValue(parameters));
                 parsedParameters.put("RSSI", getFloatValue(parameters));
                 parsedParameters.put("Ch type", getIntegerValue(parameters));
                 parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                 parsedParameters.put("CPICH SIR", getFloatValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     DPROF("DPROF") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("Channel", getIntegerValue(parameters));
                 parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                 parsedParameters.put("Ch type", getIntegerValue(parameters));
                 parsedParameters.put("#Samples", getFloatValue(parameters));
                 parsedParameters.put("Sample offset", getFloatValue(parameters));
                 parsedParameters.put("Sample", getFloatValue(parameters));
 
             }
             return parsedParameters;
         }
     },
 
     DVBRATE("DVBRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (65 == system) {
                     parsedParameters.put("DVB-H rate", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     FREQSCAN("FREQSCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("RX level", getFloatValue(parameters));
                     parsedParameters.put("C/I", getFloatValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
 
                 } else if (6 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                 } else if (11 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Carrier", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                 } else if (51 == system || 52 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("SAT", getIntegerValue(parameters));
                     parsedParameters.put("RX level", getFloatValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("DCC", getIntegerValue(parameters));
                     parsedParameters.put("RX level", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SPECTRUMSCAN("SPECTRUMSCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Scanning mode";
                 final Integer mode = getIntegerValue(parameters);
                 parsedParameters.put(key, mode);
                 if (1 == mode) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Bandwidth", getFloatValue(parameters));
                     parsedParameters.put("Sweep bandwidth", getFloatValue(parameters));
                     parsedParameters.put("Sweep frequency", getFloatValue(parameters));
                     parsedParameters.put("Sweep total RX level", getFloatValue(parameters));
                     parsedParameters.put("#frequencies", getIntegerValue(parameters));
                     parsedParameters.put("#Params/frequency", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("RX level", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     PILOTSCAN("PILOTSCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Scr.", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("RSCP", getFloatValue(parameters));
                     parsedParameters.put("SIR", getFloatValue(parameters));
                     parsedParameters.put("Delay", getFloatValue(parameters));
                     parsedParameters.put("Delay spread", getFloatValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Channel type", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Cell params ID", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("Time offset", getFloatValue(parameters));
                     parsedParameters.put("SIR", getFloatValue(parameters));
                     parsedParameters.put("RSCP", getFloatValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("Delay", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("Delay", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     OFDMSCAN("OFDMSCAN") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (25 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#frequencies", getIntegerValue(parameters));
                     parsedParameters.put("#Params/frequency", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("#Preambles", getIntegerValue(parameters));
                     parsedParameters.put("#Params/preamble", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("Preamble index", getIntegerValue(parameters));
                     parsedParameters.put("Preamble RSSI", getFloatValue(parameters));
                     parsedParameters.put("CINR", getFloatValue(parameters));
                     parsedParameters.put("Delay", getFloatValue(parameters));
                 } else if (65 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#frequencies", getIntegerValue(parameters));
                     parsedParameters.put("#Params/frequency", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("MER", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
    TPROFSCAN("TPROFSCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("RSSI", getFloatValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("#params/sample", getIntegerValue(parameters));
                     parsedParameters.put("#Samples", getIntegerValue(parameters));
                     parsedParameters.put("Chip", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     DPROFSCAN("DPROFSCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Scr.", getIntegerValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("#params/sample", getIntegerValue(parameters));
                     parsedParameters.put("#Samples", getIntegerValue(parameters));
                     parsedParameters.put("Sample offset", getFloatValue(parameters));
                     parsedParameters.put("Sample", getFloatValue(parameters));
                 } else if (10 == system || 11 == system || 12 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#params/sample", getIntegerValue(parameters));
                     parsedParameters.put("#Samples", getIntegerValue(parameters));
                     parsedParameters.put("Sample offset", getFloatValue(parameters));
                     parsedParameters.put("Sample energy", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     FINGER("FINGER") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Fingers", getIntegerValue(parameters));
                     parsedParameters.put("#params/finger", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Scr.", getIntegerValue(parameters));
                     parsedParameters.put("Secondary scr.", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("Finger abs. offset", getFloatValue(parameters));
                     parsedParameters.put("Finger rel. offset", getFloatValue(parameters));
                     parsedParameters.put("Finger RSCP", getFloatValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Agg. Ec/I0", getFloatValue(parameters));
                     parsedParameters.put("Ant. config", getIntegerValue(parameters));
                     parsedParameters.put("#Fingers", getIntegerValue(parameters));
                     parsedParameters.put("#params/finger", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Finger abs. offset", getFloatValue(parameters));
                     parsedParameters.put("Finger locked", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("Ref. finger", getIntegerValue(parameters));
                     parsedParameters.put("Assigned finger", getIntegerValue(parameters));
                     parsedParameters.put("TD mode", getIntegerValue(parameters));
                     parsedParameters.put("TD power", getFloatValue(parameters));
                     parsedParameters.put("Subchannel", getIntegerValue(parameters));
                     parsedParameters.put("Locked antennas", getIntegerValue(parameters));
                     parsedParameters.put("RX0 Ec/I0", getFloatValue(parameters));
                     parsedParameters.put("RX1 Ec/I0", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Searcher state", getIntegerValue(parameters));
                     parsedParameters.put("MSTR", getIntegerValue(parameters));
                     parsedParameters.put("MSTR error", getIntegerValue(parameters));
                     parsedParameters.put("MSTR PN", getIntegerValue(parameters));
                     parsedParameters.put("Ant. config", getIntegerValue(parameters));
                     parsedParameters.put("#Fingers", getIntegerValue(parameters));
                     parsedParameters.put("#params/finger", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Finger index", getIntegerValue(parameters));
                     parsedParameters.put("RPC cell index", getIntegerValue(parameters));
                     parsedParameters.put("ASP index", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getFloatValue(parameters));
                     parsedParameters.put("RX0 Ec/I0", getFloatValue(parameters));
                     parsedParameters.put("RX1 Ec/I0", getFloatValue(parameters));
                     parsedParameters.put("#params/finger", getIntegerValue(parameters));
                     parsedParameters.put("Finger locked", getIntegerValue(parameters));
                     parsedParameters.put("Finger abs. offset", getFloatValue(parameters));
                     parsedParameters.put("#params/finger", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 final Integer cycle = getIntegerValue(parameters);
                 parsedParameters.put("#Fingers", cycle);
                 final Integer cycleLen = getIntegerValue(parameters);
                 parsedParameters.put("#Params/finger", cycleLen);
                 if (cycle != null || cycleLen != null) {
                     for (int i = 0; i < cycle; i++) {
                         List<String> param = new ArrayList<String>();
                         for (int j = 0; j < cycleLen; j++) {
                             param.add(getStringValue(parameters));
                         }
                         parsedParameters.put("Fingers_" + i, param.toArray(new String[0]));
                     }
                 }
 
             }
             return parsedParameters;
         }
     },
     CISCAN("CISCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("C/I", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     UISCAN("UISCAN") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     // TODO check documentation
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("Scr.", getIntegerValue(parameters));
                     parsedParameters.put("UL interf.", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("#cells", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("SC", getIntegerValue(parameters));
                     parsedParameters.put("UL interf.", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     CELLSCAN("CELLSCAN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("MCC", getIntegerValue(parameters));
                     parsedParameters.put("MNC", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Cells", getIntegerValue(parameters));
                     parsedParameters.put("#params/cell", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("Scr.", getIntegerValue(parameters));
                     parsedParameters.put("MCC", getIntegerValue(parameters));
                     parsedParameters.put("MNC", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 final Integer cycle = getIntegerValue(parameters);
                 parsedParameters.put("#Cells", cycle);
                 final Integer cycleLen = getIntegerValue(parameters);
                 parsedParameters.put("Params/Cell", cycleLen);
                 if (cycle != null || cycleLen != null) {
                     for (int i = 0; i < cycle; i++) {
                         List<String> param = new ArrayList<String>();
                         for (int j = 0; j < cycleLen; j++) {
                             param.add(getStringValue(parameters));
                         }
                         parsedParameters.put("CELLSCAN_" + i, param.toArray(new String[0]));
                     }
                 }
             }
             return parsedParameters;
         }
     },
     HOA("HOA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Handover context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "HOA type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Current system";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("Number of current system parameters", getIntegerValue(parameters));
                 if (1 == system) {
                     parsedParameters.put("Ch number", getIntegerValue(parameters));
                     parsedParameters.put("TSL", getIntegerValue(parameters));
                 } else if (2 == system) {
                     parsedParameters.put("Ch number", getIntegerValue(parameters));
                     parsedParameters.put("TSL", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("Ch number", getIntegerValue(parameters));
                     parsedParameters.put("SC", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("Ch number", getIntegerValue(parameters));
                     parsedParameters.put("Cell params ID", getIntegerValue(parameters));
                 } else if (10 == system || 11 == system || 12 == system) {
                     parsedParameters.put("Ch number", getIntegerValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("Ch number", getIntegerValue(parameters));
                     parsedParameters.put("TSL", getIntegerValue(parameters));
                 }
                 system = getIntegerValue(parameters);
                 parsedParameters.put("Attempt. system", system);
                 parsedParameters.put("Number of attempted system parameters", system);
                 if (1 == system) {
                     parsedParameters.put("Att. ch", getIntegerValue(parameters));
                     parsedParameters.put("Att. TSL", getIntegerValue(parameters));
                 } else if (2 == system) {
                     parsedParameters.put("Att. ch", getIntegerValue(parameters));
                     parsedParameters.put("Att. TSL", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("Att. ch", getIntegerValue(parameters));
                     parsedParameters.put("Att. SC", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("Att. ch", getIntegerValue(parameters));
                     parsedParameters.put("Att. Cell params ID", getIntegerValue(parameters));
                 } else if (10 == system || 11 == system || 12 == system) {
                     parsedParameters.put("Att. ch", getIntegerValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("Att. ch", getIntegerValue(parameters));
                     parsedParameters.put("Att. TSL", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("HOA type", getIntegerValue(parameters));
                 parsedParameters.put("Channel number", getIntegerValue(parameters));
                 parsedParameters.put("TSL or SC", getIntegerValue(parameters));
                 parsedParameters.put("Current system", getIntegerValue(parameters));
                 parsedParameters.put("Att. ch", getIntegerValue(parameters));
                 parsedParameters.put("Att. TSL or Att. SC", getIntegerValue(parameters));
                 parsedParameters.put("Att. system", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     HOS("HOS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Handover context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
             }
             return parsedParameters;
         }
     },
     HOF("HOF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system || 21 == system) {
                     parsedParameters.put("RR cause", getIntegerValue(parameters));
                 } else if (2 == system || 11 == system || 53 == system) {
                     // 1 field reserved
                     getStringValue(parameters);
                 } else if (5 == system || 6 == system) {
                     parsedParameters.put("RRC cause", getIntegerValue(parameters));
                 }
                 // TODO check documentation Parameters for UMTS handover between systems
 
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("HOF cause", getIntegerValue(parameters));
             }
 
             return parsedParameters;
         }
     },
     CREL("CREL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Handover context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "Old system";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("#Params", getIntegerValue(parameters));
                 if (1 == system || 2 == system || 5 == system || 6 == system || 21 == system) {
                     parsedParameters.put("Old LAC", getIntegerValue(parameters));
                     parsedParameters.put("Old CI", getIntegerValue(parameters));
                 }
                 key = "System";
                 system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("#Params", getIntegerValue(parameters));
                 if (1 == system || 2 == system || 5 == system || 6 == system || 21 == system) {
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("CI", getIntegerValue(parameters));
                 }
 
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Old system", getIntegerValue(parameters));
                 parsedParameters.put("Old LAC", getIntegerValue(parameters));
                 parsedParameters.put("Old CI", getIntegerValue(parameters));
                 parsedParameters.put("New system", getIntegerValue(parameters));
                 parsedParameters.put("New LAC", getIntegerValue(parameters));
                 parsedParameters.put("New CI", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     SHOI("SHOI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     parsedParameters.put("SHO event", getIntegerValue(parameters));
                 }
                 // else if (system==7||system==8||system==14||system==15||system==26||system==30){
                 // }
             }
             return parsedParameters;
         }
     },
     SHO("SHO") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("SHO status", getIntegerValue(parameters));
                     parsedParameters.put("RRC cause", getIntegerValue(parameters));
                     parsedParameters.put("#SCs added", getIntegerValue(parameters));
                     parsedParameters.put("#SCs removed", getIntegerValue(parameters));
                     parsedParameters.put("Added SC", getIntegerValue(parameters));
                     parsedParameters.put("Removed SC", getIntegerValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("#Pilot added", getIntegerValue(parameters));
                     parsedParameters.put("#Pilot removed", getIntegerValue(parameters));
                     parsedParameters.put("Added PN", getIntegerValue(parameters));
                     parsedParameters.put("Remove PN", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     parsedParameters.put("SHO status", getIntegerValue(parameters));
                     parsedParameters.put("RRC cause", getIntegerValue(parameters));
                     parsedParameters.put("#SCs added", getIntegerValue(parameters));
                     parsedParameters.put("#SCs removed", getIntegerValue(parameters));
                     parsedParameters.put("Added SC", getIntegerValue(parameters));
                     parsedParameters.put("Removed SC", getIntegerValue(parameters));
                 } else if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#Pilots added", getIntegerValue(parameters));
                     parsedParameters.put("#Pilots removed", getIntegerValue(parameters));
                     parsedParameters.put("Added pilot", getIntegerValue(parameters));
                     parsedParameters.put("Removed pilot", getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     SIPU("SIPU") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("SIP msg. name", getStringValue(parameters));
                 parsedParameters.put("SIP msg. length", getIntegerValue(parameters));
                 // TODO check type
                 parsedParameters.put("SIP msg.", getStringValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     SIPD("SIPD") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("SIP msg. name", getStringValue(parameters));
                 parsedParameters.put("SIP msg. length", getIntegerValue(parameters));
                 // TODO check type
                 parsedParameters.put("SIP msg.", getStringValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     RTPU("RTPU") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("RTP msg. name", getStringValue(parameters));
                 parsedParameters.put("RTP msg. seq.#", getIntegerValue(parameters));
                 parsedParameters.put("RTP msg. length", getIntegerValue(parameters));
                 // TODO check type
                 parsedParameters.put("RTP msg.", getStringValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     RTPD("RTPD") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("RTP msg. name", getStringValue(parameters));
                 parsedParameters.put("RTP msg. seq.#", getIntegerValue(parameters));
                 parsedParameters.put("RTP msg. length", getIntegerValue(parameters));
                 // TODO check type
                 parsedParameters.put("RTP msg.", getStringValue(parameters));
 
             }
             return parsedParameters;
         }
     },
 
     LUA("LUA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Location area update context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "LAU type";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("LAU type", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     LUS("LUS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Location area update context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Old LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "MCC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "MNC";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "Old LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "New LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "MCC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "MNC";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     LUF("LUF") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Location area update context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "LUF status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Old LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("MM cause", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "LUF status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Old LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "MM cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     CHI("CHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("DTX UL", getIntegerValue(parameters));
                     parsedParameters.put("RLT max", getIntegerValue(parameters));
                     parsedParameters.put("Ext. ch type", getIntegerValue(parameters));
                     parsedParameters.put("TN", getIntegerValue(parameters));
                     parsedParameters.put("BCCH ch", getIntegerValue(parameters));
                 } else if (2 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Subchannel", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("Extended subchannel", getIntegerValue(parameters));
                     parsedParameters.put("Encryption", getIntegerValue(parameters));
                     parsedParameters.put("Slot number", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("RRC state", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("Addition window", getFloatValue(parameters));
                     parsedParameters.put("Time to trigger 1A", getIntegerValue(parameters));
                     parsedParameters.put("Drop window", getFloatValue(parameters));
                     parsedParameters.put("Time to trigger 1B", getIntegerValue(parameters));
                     parsedParameters.put("Replacement window", getFloatValue(parameters));
                     parsedParameters.put("Time to trigger 1C", getIntegerValue(parameters));
                     parsedParameters.put("DL SF", getIntegerValue(parameters));
                     parsedParameters.put("Min UL SF", getIntegerValue(parameters));
                     parsedParameters.put("DRX cycle", getIntegerValue(parameters));
                     parsedParameters.put("Max TX power", getFloatValue(parameters));
                     parsedParameters.put("DRX cycle", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("RRC state", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("DRX cycle", getIntegerValue(parameters));
                     parsedParameters.put("Max TX power", getFloatValue(parameters));
                     parsedParameters.put("Treselection", getIntegerValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("MCC", getIntegerValue(parameters));
                     parsedParameters.put("SID (System ID)", getIntegerValue(parameters));
                     parsedParameters.put("NID (Network ID)", getIntegerValue(parameters));
                     parsedParameters.put("Slotted mode", getIntegerValue(parameters));
                     parsedParameters.put("SEARCH_WIN_A", getIntegerValue(parameters));
                     parsedParameters.put("SEARCH_WIN_N", getIntegerValue(parameters));
                     parsedParameters.put("SEARCH_WIN_R", getIntegerValue(parameters));
                     parsedParameters.put("T_ADD", getIntegerValue(parameters));
                     parsedParameters.put("T_DROP", getIntegerValue(parameters));
                     parsedParameters.put("T_TDROP", getIntegerValue(parameters));
                     parsedParameters.put("T_COMP", getIntegerValue(parameters));
                     parsedParameters.put("P_REV", getIntegerValue(parameters));
                     parsedParameters.put("MIN_P_REV", getIntegerValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                 } else if (21 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Cell ID", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("MAC state", getIntegerValue(parameters));
                     parsedParameters.put("Frequency", getFloatValue(parameters));
                     parsedParameters.put("BS ID", getStringValue(parameters));
                     parsedParameters.put("FFT Size", getIntegerValue(parameters));
                     parsedParameters.put("Bandwidth", getFloatValue(parameters));
                     parsedParameters.put("Frame Ratio DL", getIntegerValue(parameters));
                     parsedParameters.put("Frame Ratio UL", getIntegerValue(parameters));
                     parsedParameters.put("MAP coding", getIntegerValue(parameters));
                     parsedParameters.put("MAP repetition", getIntegerValue(parameters));
                 } else if (51 == system || 52 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                 } else if (53 == system) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Ch", getIntegerValue(parameters));
                     parsedParameters.put("NW type", getIntegerValue(parameters));
                     parsedParameters.put("PSID1", getIntegerValue(parameters));
                     parsedParameters.put("PSID2", getIntegerValue(parameters));
                     parsedParameters.put("PSID3", getIntegerValue(parameters));
                     parsedParameters.put("PSID4", getIntegerValue(parameters));
                     parsedParameters.put("LAREG", getIntegerValue(parameters));
                     parsedParameters.put("RNUM", getIntegerValue(parameters));
                     parsedParameters.put("REG PERIOD", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("CI", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     // reserved 1 field
                     getStringValue(parameters);
                     parsedParameters.put("DTX UL", getIntegerValue(parameters));
                     parsedParameters.put("RTL max", getIntegerValue(parameters));
                     parsedParameters.put("Ext. ch type", getIntegerValue(parameters));
                     parsedParameters.put("TN", getIntegerValue(parameters));
                     parsedParameters.put("BCCH ARFCN", getIntegerValue(parameters));
                 } else if (system == 4 || system == 9) {
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                 } else if (system == 5 || system == 6) {
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("NW type", getIntegerValue(parameters));
                     parsedParameters.put("PSID1", getIntegerValue(parameters));
                     parsedParameters.put("PSID2", getIntegerValue(parameters));
                     parsedParameters.put("PSID3", getIntegerValue(parameters));
                     parsedParameters.put("PSID4", getIntegerValue(parameters));
                     parsedParameters.put("LAREG", getIntegerValue(parameters));
                     parsedParameters.put("RNUM", getIntegerValue(parameters));
                     parsedParameters.put("REG PERIOD", getIntegerValue(parameters));
                 } else if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Carrier", getIntegerValue(parameters));
                     parsedParameters.put("MCC", getIntegerValue(parameters));
                     parsedParameters.put("SID", getIntegerValue(parameters));
                     parsedParameters.put("NID", getIntegerValue(parameters));
                     parsedParameters.put("Slotted mode", getIntegerValue(parameters));
                     parsedParameters.put("SEARCH_WIN_A", getIntegerValue(parameters));
                     parsedParameters.put("SEARCH_WIN_N", getIntegerValue(parameters));
                     parsedParameters.put("SEARCH_WIN_R", getIntegerValue(parameters));
                     parsedParameters.put("T_ADD", getIntegerValue(parameters));
                     parsedParameters.put("T_DROP", getIntegerValue(parameters));
                     parsedParameters.put("T_TDROP", getIntegerValue(parameters));
                     parsedParameters.put("T_COMP", getIntegerValue(parameters));
                     parsedParameters.put("P_REV", getIntegerValue(parameters));
                     parsedParameters.put("MIN_P_REV", getIntegerValue(parameters));
                 } else if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33
                         || system == 34) {
                     parsedParameters.put("RRC state", getIntegerValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("CI", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("Addition window", getFloatValue(parameters));
                     parsedParameters.put("1A time to tr.", getIntegerValue(parameters));
                     parsedParameters.put("Drop window", getFloatValue(parameters));
                     parsedParameters.put("1B time to tr.", getIntegerValue(parameters));
                     parsedParameters.put("Repl. window", getFloatValue(parameters));
                     parsedParameters.put("1C time to tr.", getIntegerValue(parameters));
                     parsedParameters.put("DL SF", getIntegerValue(parameters));
                     parsedParameters.put("Min UL SF", getIntegerValue(parameters));
                     parsedParameters.put("DRX cycle", getIntegerValue(parameters));
                     parsedParameters.put("Max TX power", getIntegerValue(parameters));
                     parsedParameters.put("Treselection", getIntegerValue(parameters));
                 } else if (system == 11) {
                     parsedParameters.put("Ch type", getIntegerValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("Band", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                     parsedParameters.put("Ext. ch type", getIntegerValue(parameters));
                     parsedParameters.put("Encryption", getIntegerValue(parameters));
                     parsedParameters.put("Slot number", getIntegerValue(parameters));
                 } else if (system == 32) {
                     parsedParameters.put("CI", getIntegerValue(parameters));
                     parsedParameters.put("LAC", getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
     GANCHI("GANCHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (21 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("GAN state", getIntegerValue(parameters));
                     parsedParameters.put("GAN channel", getIntegerValue(parameters));
                     parsedParameters.put("GAN BSIC", getIntegerValue(parameters));
                     parsedParameters.put("GAN CI", getIntegerValue(parameters));
                     parsedParameters.put("GAN LAC", getIntegerValue(parameters));
                     parsedParameters.put("GANC IP", getStringValue(parameters));
                     parsedParameters.put("SEGW IP", getStringValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 32) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("GAN state", getIntegerValue(parameters));
                     parsedParameters.put("GAN channel", getIntegerValue(parameters));
                     parsedParameters.put("GAN BSIC", getIntegerValue(parameters));
                     parsedParameters.put("GAN CI", getIntegerValue(parameters));
                     parsedParameters.put("GAN LAC", getIntegerValue(parameters));
                     parsedParameters.put("GAN IP", getStringValue(parameters));
                     parsedParameters.put("SEGW IP", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SEI("SEI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Service status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("LAC", getIntegerValue(parameters));
                 parsedParameters.put("MCC", getIntegerValue(parameters));
                 parsedParameters.put("MNC", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Service status", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     ROAM("ROAM") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Roaming status";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Service status", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     DCHR("DCHR") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Initiator", getIntegerValue(parameters));
                     parsedParameters.put("Coding", getIntegerValue(parameters));
                     parsedParameters.put("Data mode", getIntegerValue(parameters));
                     parsedParameters.put("#CS TSL UL", getIntegerValue(parameters));
                     parsedParameters.put("#CS TSL DL", getIntegerValue(parameters));
                     parsedParameters.put("Modem type", getIntegerValue(parameters));
                     parsedParameters.put("Compression", getStringValue(parameters));
                 } else if (5 == system || 6 == system) {
                     parsedParameters.put("Initiator", getIntegerValue(parameters));
                     parsedParameters.put("Req. CS rate", getIntegerValue(parameters));
                     parsedParameters.put("Data mode", getIntegerValue(parameters));
                     parsedParameters.put("Modem type", getIntegerValue(parameters));
                     parsedParameters.put("Compression", getStringValue(parameters));
                 }
 
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("Initiator", getIntegerValue(parameters));
                     parsedParameters.put("Coding", getIntegerValue(parameters));
                     parsedParameters.put("Data mode", getIntegerValue(parameters));
                     parsedParameters.put("#CS TSL UL", getIntegerValue(parameters));
                     parsedParameters.put("#CS TSL DL", getIntegerValue(parameters));
                     parsedParameters.put("Modem type", getIntegerValue(parameters));
                     parsedParameters.put("Compression", getStringValue(parameters));
                 } else if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33
                         || system == 34) {
                     parsedParameters.put("Initiator", getIntegerValue(parameters));
                     parsedParameters.put("Req. CS rate", getIntegerValue(parameters));
                     parsedParameters.put("Data mode", getIntegerValue(parameters));
                     parsedParameters.put("Modem type", getIntegerValue(parameters));
                     parsedParameters.put("Compression", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     DCHI("DCHI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("Coding", getIntegerValue(parameters));
                 parsedParameters.put("Data mode", getIntegerValue(parameters));
                 parsedParameters.put("#CS TSL UL", getIntegerValue(parameters));
                 parsedParameters.put("#CS TSL DL", getIntegerValue(parameters));
                 parsedParameters.put("CS TNs UL", getIntegerValue(parameters));
                 parsedParameters.put("CS TNs DL", getStringValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("Coding", getIntegerValue(parameters));
                 parsedParameters.put("Data mode", getIntegerValue(parameters));
                 parsedParameters.put("#CS TSL UL", getIntegerValue(parameters));
                 parsedParameters.put("#CS TSL DL", getIntegerValue(parameters));
                 parsedParameters.put("CS TNs UL", getIntegerValue(parameters));
                 parsedParameters.put("CS TNs DL", getStringValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     HOP("HOP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 final Integer hopping = getIntegerValue(parameters);
                 if (1 == system) {
                     parsedParameters.put("Hopping", hopping);
                     if (hopping == 1) {
                         parsedParameters.put("HSN", getIntegerValue(parameters));
                         parsedParameters.put("MAIO", getIntegerValue(parameters));
                         parsedParameters.put("#Hopping Chs", getIntegerValue(parameters));
                         parsedParameters.put("Channel(s)", getIntegerValue(parameters));
                     } else {
                         parsedParameters.put("Channel(s)", getIntegerValue(parameters));
                     }
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 final Integer hopping = getIntegerValue(parameters);
                 parsedParameters.put("Hopping", hopping);
                 if (hopping == 1) {
                     parsedParameters.put("HSN", getIntegerValue(parameters));
                     parsedParameters.put("MAIO", getIntegerValue(parameters));
                     parsedParameters.put("Hopping ch", getIntegerValue(parameters));
                 } else {
                     parsedParameters.put("Traffic ARFCN", getIntegerValue(parameters));
                 }
 
             }
             return parsedParameters;
         }
     },
 
     NLIST("NLIST") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Source system";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Source band", getIntegerValue(parameters));
                     parsedParameters.put("Source ch", getIntegerValue(parameters));
                     parsedParameters.put("Source BSIC", getIntegerValue(parameters));
 
                 } else if (5 == system) {
                     parsedParameters.put("Source band", getIntegerValue(parameters));
                     parsedParameters.put("Source ch", getIntegerValue(parameters));
                     parsedParameters.put("Source SC", getIntegerValue(parameters));
 
                 } else if (6 == system) {
                     parsedParameters.put("Source band", getIntegerValue(parameters));
                     parsedParameters.put("Source ch", getIntegerValue(parameters));
                     parsedParameters.put("Source params ID", getIntegerValue(parameters));
 
                 }
                 if (!parameters.hasNext()) {
                     return parsedParameters;// TODO debug
                 }
                 parsedParameters.put("#nChs", getIntegerValue(parameters));
                 parsedParameters.put("#Params", getIntegerValue(parameters));
                 system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (1 == system) {
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("UARFCN", getIntegerValue(parameters));
                     parsedParameters.put("SC", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("UARFCN", getIntegerValue(parameters));
                     parsedParameters.put("UARFCN ch", getIntegerValue(parameters));
 
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("Nlist status", getIntegerValue(parameters));
                 parsedParameters.put("#nChs", getIntegerValue(parameters));
                 parsedParameters.put("#Params", getIntegerValue(parameters));
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                 } else if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33
                         || system == 34) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     NMISS("NMISS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Source system";
                 Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Source band", getIntegerValue(parameters));
                     parsedParameters.put("Source ch", getIntegerValue(parameters));
                     parsedParameters.put("Source BSIC", getIntegerValue(parameters));
 
                 } else if (5 == system) {
                     parsedParameters.put("Source band", getIntegerValue(parameters));
                     parsedParameters.put("Source ch", getIntegerValue(parameters));
                     parsedParameters.put("Source SC", getIntegerValue(parameters));
 
                 } else if (6 == system) {
                     parsedParameters.put("Source band", getIntegerValue(parameters));
                     parsedParameters.put("Source ch", getIntegerValue(parameters));
                     parsedParameters.put("Source params ID", getIntegerValue(parameters));
 
                 }
                 parsedParameters.put("#nChs", getIntegerValue(parameters));
                 parsedParameters.put("#Params", getIntegerValue(parameters));
                 system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (1 == system) {
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("UARFCN", getIntegerValue(parameters));
                     parsedParameters.put("SC", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("UARFCN", getIntegerValue(parameters));
                     parsedParameters.put("UARFCN ch", getIntegerValue(parameters));
 
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("#nChs", getIntegerValue(parameters));
                 parsedParameters.put("#Params", getIntegerValue(parameters));
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("RX level", getIntegerValue(parameters));
                 } else if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33
                         || system == 34) {
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("Scrambling code", getIntegerValue(parameters));
                     parsedParameters.put("Ec/N0", getIntegerValue(parameters));
                     parsedParameters.put("RSCP", getIntegerValue(parameters));
                     parsedParameters.put("Diff. tp st", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SEPR("SEPR") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     parsedParameters.put("Service option", getIntegerValue(parameters));
                     parsedParameters.put("Req. forw. RC", getIntegerValue(parameters));
                     parsedParameters.put("Req. rev. RC", getIntegerValue(parameters));
                     parsedParameters.put("Req. F-FCH MUX", getIntegerValue(parameters));
                     parsedParameters.put("Req. R-FCH MUX", getIntegerValue(parameters));
 
                 }
             }
             return parsedParameters;
         }
     },
     SEPN("SEPN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     parsedParameters.put("Neg. SO", getIntegerValue(parameters));
                     parsedParameters.put("Neg. forw. RC", getIntegerValue(parameters));
                     parsedParameters.put("Neg. rev. RC", getIntegerValue(parameters));
                     parsedParameters.put("Neg. F-FCH MUX", getIntegerValue(parameters));
                     parsedParameters.put("Neg. R-FCH MUX", getIntegerValue(parameters));
 
                 }
             }
             return parsedParameters;
         }
     },
     SERVCONF("SERVCONF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (10 == system || 11 == system) {
                     parsedParameters.put("SO", getIntegerValue(parameters));
                     parsedParameters.put("FT type", getIntegerValue(parameters));
                     parsedParameters.put("RT type", getIntegerValue(parameters));
                     parsedParameters.put("Encryption mode", getIntegerValue(parameters));
                     parsedParameters.put("F-FCH MUX", getIntegerValue(parameters));
                     parsedParameters.put("R-FCH MUX", getIntegerValue(parameters));
                     parsedParameters.put("F-FCH bits/frame", getIntegerValue(parameters));
                     parsedParameters.put("R-FCH bits/frame", getIntegerValue(parameters));
                     parsedParameters.put("F-FCH RC", getIntegerValue(parameters));
                     parsedParameters.put("R-FCH RC", getIntegerValue(parameters));
                     parsedParameters.put("F-DCCH RC", getIntegerValue(parameters));
                     parsedParameters.put("R-DCCH radio configuration", getIntegerValue(parameters));
                     parsedParameters.put("F-SCH MUX", getIntegerValue(parameters));
                     parsedParameters.put("F-SCH RC", getIntegerValue(parameters));
                     parsedParameters.put("F-SCH coding", getIntegerValue(parameters));
                     parsedParameters.put("F-SCH frame size", getIntegerValue(parameters));
                     parsedParameters.put("F-SCH frame offset", getIntegerValue(parameters));
                     parsedParameters.put("F-SCH max rate", getIntegerValue(parameters));
                     parsedParameters.put("R-SCH MUX", getIntegerValue(parameters));
                     parsedParameters.put("R-SCH RC", getIntegerValue(parameters));
                     parsedParameters.put("R-SCH coding", getIntegerValue(parameters));
                     parsedParameters.put("R-SCH frame size", getIntegerValue(parameters));
                     parsedParameters.put("R-SCH frame offset", getIntegerValue(parameters));
                     parsedParameters.put("R-SCH max rate", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RACHI("RACHI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("Init TX power", getFloatValue(parameters));
                     parsedParameters.put("Preamble step", getFloatValue(parameters));
                     parsedParameters.put("Preamble count", getIntegerValue(parameters));
                     parsedParameters.put("RACH TX power", getFloatValue(parameters));
                     parsedParameters.put("Max preamble", getIntegerValue(parameters));
                     parsedParameters.put("UL interf.", getFloatValue(parameters));
                     parsedParameters.put("AICH status", getIntegerValue(parameters));
                     parsedParameters.put("Data gain", getIntegerValue(parameters));
                     parsedParameters.put("Ctrl gain", getIntegerValue(parameters));
                     parsedParameters.put("Power offset", getFloatValue(parameters));
                     parsedParameters.put("Message length", getIntegerValue(parameters));
                     parsedParameters.put("Preamble cycles", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("SYNC UL Init. pwr.", getFloatValue(parameters));
                     parsedParameters.put("SYNC UL step", getFloatValue(parameters));
                     parsedParameters.put("SYNC UL count", getIntegerValue(parameters));
                     parsedParameters.put("Max SYNC UL count", getIntegerValue(parameters));
                     parsedParameters.put("SYNC UL power", getFloatValue(parameters));
                     parsedParameters.put("RACH TX power", getFloatValue(parameters));
                     parsedParameters.put("PCCPCH pathloss", getFloatValue(parameters));
                     parsedParameters.put("RACH status", getIntegerValue(parameters));
                     parsedParameters.put("Desired UpPCH RX power", getFloatValue(parameters));
                     parsedParameters.put("Desired UpRACH RX power", getFloatValue(parameters));
                     parsedParameters.put("Message length", getIntegerValue(parameters));
                     parsedParameters.put("Preamble cycles", getIntegerValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("NOM_PWR", getIntegerValue(parameters));
                     parsedParameters.put("INIT_PWR", getIntegerValue(parameters));
                     parsedParameters.put("PWR_STEP", getIntegerValue(parameters));
                     parsedParameters.put("NUM_STEP", getIntegerValue(parameters));
                     parsedParameters.put("TX level", getFloatValue(parameters));
                     parsedParameters.put("Probe count", getIntegerValue(parameters));
                     parsedParameters.put("Probe seq. count", getIntegerValue(parameters));
                     parsedParameters.put("Access ch number", getIntegerValue(parameters));
                     parsedParameters.put("Random delay", getIntegerValue(parameters));
                     parsedParameters.put("Access RX level", getFloatValue(parameters));
                     parsedParameters.put("Psist", getIntegerValue(parameters));
                     parsedParameters.put("Seq. backoff", getIntegerValue(parameters));
                     parsedParameters.put("Prob. backoff", getIntegerValue(parameters));
                     parsedParameters.put("Inter. corr.", getIntegerValue(parameters));
                     parsedParameters.put("Access TX adj.", getFloatValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("Max #Probes", getIntegerValue(parameters));
                     parsedParameters.put("Max #Probe seqs", getIntegerValue(parameters));
                     parsedParameters.put("Result", getIntegerValue(parameters));
                     parsedParameters.put("#Probes", getIntegerValue(parameters));
                     parsedParameters.put("#Probe seqs", getFloatValue(parameters));
                     parsedParameters.put("Duration", getIntegerValue(parameters));
                     parsedParameters.put("Access PN", getIntegerValue(parameters));
                     parsedParameters.put("Access ch number", getIntegerValue(parameters));
                     parsedParameters.put("Access sector id", getIntegerValue(parameters));
                     parsedParameters.put("Access color code", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 12 || system == 13 || system == 20 || system == 21 || system == 31 || system == 33 || system == 34) {
                     parsedParameters.put("Init. TX pwr.", getIntegerValue(parameters));
                     parsedParameters.put("Preamble step", getIntegerValue(parameters));
                     parsedParameters.put("Preamble count", getIntegerValue(parameters));
                     parsedParameters.put("RACX TX power", getIntegerValue(parameters));
                     parsedParameters.put("Max preamble", getIntegerValue(parameters));
                     parsedParameters.put("UL interf.", getIntegerValue(parameters));
                     parsedParameters.put("AICH status", getIntegerValue(parameters));
                     parsedParameters.put("Data gain", getIntegerValue(parameters));
                     parsedParameters.put("Ctrl gain", getIntegerValue(parameters));
                     parsedParameters.put("Power offset", getIntegerValue(parameters));
                     parsedParameters.put("Message length", getIntegerValue(parameters));
                     parsedParameters.put("Preamble cycles", getIntegerValue(parameters));
                 } else if (system == 7 || system == 8 || system == 14 || system == 15 || system == 26 || system == 30) {
                     parsedParameters.put("NOM_PWR", getIntegerValue(parameters));
                     parsedParameters.put("INIT_PWR", getIntegerValue(parameters));
                     parsedParameters.put("PWR_STEP", getIntegerValue(parameters));
                     parsedParameters.put("NUM_STEP", getIntegerValue(parameters));
                     parsedParameters.put("TX level", getFloatValue(parameters));
                     parsedParameters.put("Access probe count max", getIntegerValue(parameters));
                     parsedParameters.put("Access probe seq. max", getIntegerValue(parameters));
                     parsedParameters.put("Result", getIntegerValue(parameters));
                     parsedParameters.put("Access ch", getIntegerValue(parameters));
                     parsedParameters.put("Random delay", getIntegerValue(parameters));
                 } else if (system == 27 || system == 28 || system == 29) {
                     parsedParameters.put("MAX #Probes", getIntegerValue(parameters));
                     parsedParameters.put("MAX #Probe seqs", getIntegerValue(parameters));
                     parsedParameters.put("Result", getIntegerValue(parameters));
                     parsedParameters.put("#Probes", getIntegerValue(parameters));
                     parsedParameters.put("#Probe seqs", getIntegerValue(parameters));
                 }
             }
 
             return parsedParameters;
         }
     },
     VOCS("VOCS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Voc. rate For.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Voc. rate Rev.";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
 
         }
     },
     PHCHI("PHCHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (6==system){
                     parsedParameters.put("#Header params", getIntegerValue(parameters));                   
                     parsedParameters.put("DPCH ch", getIntegerValue(parameters));                   
                     parsedParameters.put("UL repetition length", getIntegerValue(parameters));                   
                     parsedParameters.put("UL repetition period", getIntegerValue(parameters));                   
                     parsedParameters.put("DL repetition length", getIntegerValue(parameters));                   
                     parsedParameters.put("DL repetition period", getIntegerValue(parameters));                   
                     parsedParameters.put("#Physical channels", getIntegerValue(parameters));                   
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));                   
                     parsedParameters.put("Ph. ch. type", getIntegerValue(parameters));                   
                     parsedParameters.put("TSL", getIntegerValue(parameters));                   
                     parsedParameters.put("SF", getIntegerValue(parameters));                   
                     parsedParameters.put("Ch. code", getIntegerValue(parameters));                   
                     parsedParameters.put("Midamble config", getIntegerValue(parameters));                   
                     parsedParameters.put("Midamble shift", getIntegerValue(parameters));                   
 
                 }else if (10==system||11==system){
                     parsedParameters.put("#Header params", getIntegerValue(parameters));                   
                     parsedParameters.put("#Physical channels", getIntegerValue(parameters));                   
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));                   
                     parsedParameters.put("Ph. type", getIntegerValue(parameters));                   
                     parsedParameters.put("Direction", getIntegerValue(parameters));                   
                     parsedParameters.put("PN", getIntegerValue(parameters));                   
                     parsedParameters.put("Walsh code", getIntegerValue(parameters));                   
                     parsedParameters.put("Ph. rate", getIntegerValue(parameters));                   
                     parsedParameters.put("QOF mask id", getIntegerValue(parameters));                   
 
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("#Header params", getIntegerValue(parameters));
                 parsedParameters.put("#Physical channels", getIntegerValue(parameters));
                 parsedParameters.put("#Params/channel", getIntegerValue(parameters));
                 parsedParameters.put("Type", getIntegerValue(parameters));
                 parsedParameters.put("Direction", getIntegerValue(parameters));
                 parsedParameters.put("Pilot PN", getIntegerValue(parameters));
                 parsedParameters.put("Walsh code", getIntegerValue(parameters));
                 parsedParameters.put("Rate", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     QPCHI("QPCHI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                     String key = "System";
                     final Integer system = getIntegerValue(parameters);
                     parsedParameters.put(key, system);
                     if (11==system){
                         parsedParameters.put("#Header params", getIntegerValue(parameters));                   
                         parsedParameters.put("Rate", getIntegerValue(parameters));                   
                         parsedParameters.put("Slot number", getIntegerValue(parameters));                   
                         parsedParameters.put("Transfer reason", getIntegerValue(parameters));                   
                         parsedParameters.put("#Configurations", getIntegerValue(parameters));                   
                         parsedParameters.put("#Params/configuration", getIntegerValue(parameters));                   
                         parsedParameters.put("PN", getIntegerValue(parameters));                   
                         parsedParameters.put("PI walsh", getIntegerValue(parameters));                   
                         parsedParameters.put("PI power offset", getFloatValue(parameters));
                         parsedParameters.put("BI supported", getIntegerValue(parameters));
                         parsedParameters.put("BI walsh", getIntegerValue(parameters));
                         parsedParameters.put("BI pwr lvl", getFloatValue(parameters));                   
                         parsedParameters.put("CCI supported", getIntegerValue(parameters));                   
                         parsedParameters.put("CCI walsh", getIntegerValue(parameters));                   
                         parsedParameters.put("CCI pwr lvl", getFloatValue(parameters));                   
                         parsedParameters.put("#Indicators", getIntegerValue(parameters));                   
                         parsedParameters.put("#Params/indicator", getIntegerValue(parameters));                   
                         parsedParameters.put("Status", getIntegerValue(parameters));                   
                         parsedParameters.put("Type", getIntegerValue(parameters));                   
                         parsedParameters.put("THB", getIntegerValue(parameters));                   
                         parsedParameters.put("THI", getIntegerValue(parameters));                   
                         parsedParameters.put("Position", getIntegerValue(parameters));                   
                         parsedParameters.put("Ind. I amp.", getIntegerValue(parameters));                   
                         parsedParameters.put("Ind. Q amp.", getIntegerValue(parameters));                   
                         parsedParameters.put("Com. pilot energy", getFloatValue(parameters));                   
                         parsedParameters.put("Div. pilot energy", getFloatValue(parameters));                   
                     }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("QPCH rate", getIntegerValue(parameters));
                 parsedParameters.put("Slot number", getIntegerValue(parameters));
                 parsedParameters.put("QPCH pilot#", getIntegerValue(parameters));
                 parsedParameters.put("PI Walsh", getIntegerValue(parameters));
                 parsedParameters.put("PI power offset", getFloatValue(parameters));
                 parsedParameters.put("THB", getIntegerValue(parameters));
                 parsedParameters.put("THI", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     FCHPACKETS("FCHPACKETS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("CC38400 Good", getIntegerValue(parameters));
                     parsedParameters.put("CC38400 Bad", getIntegerValue(parameters));
                     parsedParameters.put("CC76800 Good", getIntegerValue(parameters));
                     parsedParameters.put("CC76800 Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC38400 Good", getIntegerValue(parameters));
                     parsedParameters.put("TC38400 Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC76800 Good", getIntegerValue(parameters));
                     parsedParameters.put("TC76800 Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC153600 Good", getIntegerValue(parameters));
                     parsedParameters.put("TC153600 Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC307200Short Good", getIntegerValue(parameters));
                     parsedParameters.put("TC307200Short Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC307200Long Good", getIntegerValue(parameters));
                     parsedParameters.put("TC307200Long Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC614400Short Good", getIntegerValue(parameters));
                     parsedParameters.put("TC614400Short Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC614400Long Good", getIntegerValue(parameters));
                     parsedParameters.put("TC614400Long Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC921600 Good", getIntegerValue(parameters));
                     parsedParameters.put("TC921600 Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC1228800Short Good", getIntegerValue(parameters));
                     parsedParameters.put("TC1228800Short Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC1228800Long Good", getIntegerValue(parameters));
                     parsedParameters.put("TC1228800Long Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC1843200 Good", getIntegerValue(parameters));
                     parsedParameters.put("TC1843200 Bad", getIntegerValue(parameters));
                     parsedParameters.put("TC2457600 Good", getIntegerValue(parameters));
                     parsedParameters.put("TC2457600 Bad", getIntegerValue(parameters));
 
                 }
             }
             return parsedParameters;
         }
     },
     CONNECTIONC("CONNECTIONC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("Transaction ID", getIntegerValue(parameters));
                     parsedParameters.put("Message seq.", getIntegerValue(parameters));
                     parsedParameters.put("Connection result", getIntegerValue(parameters));
                     parsedParameters.put("Rec. status", getIntegerValue(parameters));
                     parsedParameters.put("Duration", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("Sector ID", getIntegerValue(parameters));
                     parsedParameters.put("CC", getIntegerValue(parameters));
                     parsedParameters.put("#PN changes", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     CONNECTIOND("CONNECTIOND") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("Reason", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SESSIONC("SESSIONC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("Transaction ID", getIntegerValue(parameters));
                     parsedParameters.put("Result", getIntegerValue(parameters));
                     parsedParameters.put("RATI", getIntegerValue(parameters));
                     parsedParameters.put("Duration", getIntegerValue(parameters));
                     parsedParameters.put("PN", getIntegerValue(parameters));
                     parsedParameters.put("CC", getIntegerValue(parameters));
                     parsedParameters.put("Full UATI", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RBI("RBI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#params/RB";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#RBs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 if (5 == system) {
                     parsedParameters.put("RB ID", getIntegerValue(parameters));
                     parsedParameters.put("RLC ID", getIntegerValue(parameters));
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("Logical Ch", getIntegerValue(parameters));
                     parsedParameters.put("RLC mode", getIntegerValue(parameters));
                     parsedParameters.put("Radio bearer ciphering", getIntegerValue(parameters));
                     parsedParameters.put("TrCh type", getIntegerValue(parameters));
                 }else if (6 == system) {
                     parsedParameters.put("RB ID", getIntegerValue(parameters));
                     parsedParameters.put("RLC ID", getIntegerValue(parameters));
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("Logical Ch", getIntegerValue(parameters));
                     parsedParameters.put("RLC mode", getIntegerValue(parameters));
                     parsedParameters.put("Radio bearer ciphering", getIntegerValue(parameters));
                     parsedParameters.put("TrCh type", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#params/RB";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#RBs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("RB ID", getIntegerValue(parameters));
                 parsedParameters.put("RLC ID", getIntegerValue(parameters));
                 parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                 parsedParameters.put("Direction", getIntegerValue(parameters));
                 parsedParameters.put("Logical Ch", getIntegerValue(parameters));
                 parsedParameters.put("RLC mode", getIntegerValue(parameters));
                 parsedParameters.put("Chiphering", getIntegerValue(parameters));
                 parsedParameters.put("TrCh type", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     TRCHI("TRCHI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#params/TRCH";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#TrChs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 if (5 == system) {
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("CCTrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("TrCh type", getIntegerValue(parameters));
                     parsedParameters.put("TrCh coding", getIntegerValue(parameters));
                     parsedParameters.put("CRC length", getIntegerValue(parameters));
                     parsedParameters.put("TTI", getIntegerValue(parameters));
                     parsedParameters.put("Rate-m. attr.", getIntegerValue(parameters));
                 }else if (6 == system){
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("CCTrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("TrCh type", getIntegerValue(parameters));
                     parsedParameters.put("TrCh coding", getIntegerValue(parameters));
                     parsedParameters.put("CRC length", getIntegerValue(parameters));
                     parsedParameters.put("TTI", getIntegerValue(parameters));
                     parsedParameters.put("Rate-m. attr.", getIntegerValue(parameters));                   
                 }
             } else if ("1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#params/TRCH";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#TrChs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("TrChs ID", getIntegerValue(parameters));
                 parsedParameters.put("CCTrChs ID", getIntegerValue(parameters));
                 parsedParameters.put("Direction", getIntegerValue(parameters));
                 parsedParameters.put("TrChs type", getIntegerValue(parameters));
                 parsedParameters.put("TrChs coding", getIntegerValue(parameters));
                 parsedParameters.put("CRC length", getIntegerValue(parameters));
                 parsedParameters.put("TTI", getIntegerValue(parameters));
                 parsedParameters.put("Rate-m. attr.", getIntegerValue(parameters));
 
             }
             return parsedParameters;
         }
     },
     RRA("RRA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "RRC context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("RRC est. cause", getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("RRC est. cause", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRC("RRC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "RRC context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("#RRC att.", getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("#RRC att.", getIntegerValue(parameters));
                 parsedParameters.put("RRC est. time", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRF("RRF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "RRC context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("#RRC att. abort", getIntegerValue(parameters));
                 parsedParameters.put("RRC rej. status", getIntegerValue(parameters));
                 parsedParameters.put("RRC rej. cause", getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("#RRC att.", getIntegerValue(parameters));
                 parsedParameters.put("RRC fail. time", getIntegerValue(parameters));
                 parsedParameters.put("RRC rej. status", getIntegerValue(parameters));
                 parsedParameters.put("RRC rej. cause", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRD("RRD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "RRC context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("RRC rel. status", getIntegerValue(parameters));
                 parsedParameters.put("RRC rel. cause", getIntegerValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("RRC rel. status", getIntegerValue(parameters));
                 parsedParameters.put("RRC rel. cause", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     CIPI("CIPI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 parsedParameters.put("Air encryption", getIntegerValue(parameters));
                 parsedParameters.put("KSG", getStringValue(parameters));
                 parsedParameters.put("SCK", getStringValue(parameters));
 
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 parsedParameters.put("Ciph. type", getIntegerValue(parameters));
                 parsedParameters.put("KSG", getIntegerValue(parameters));
                 parsedParameters.put("SCK", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     L3U("L3U") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel/Channel type", getStringValue(parameters));
                 parsedParameters.put("L3 msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("L3 data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     L3D("L3D") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel/Channel type", getStringValue(parameters));
                 parsedParameters.put("L3 msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("L3 data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     L2U("L2U") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("L2 msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("L2 data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     L2D("L2D") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("L2 msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("L2 data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     MACU("MACU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RLC/MAC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RLC/MAC data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     MACD("MACD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RLC/MAC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RLC/MAC data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     LLCU("LLCU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("LLC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("LLC data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     LLCD("LLCD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("LLC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("LLC data", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRLPU("RRLPU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RRLP msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RRLP data", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRLPD("RRLPD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RRLP msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RRLP data", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRCU("RRCU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RRC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RRC data", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     RRCD("RRCD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RRC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RRC data", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RLCU("RLCU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RLC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RLC data", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     RLCD("RLCD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("Subchannel", getStringValue(parameters));
                 parsedParameters.put("RLC msg", getStringValue(parameters));
                 // TODO check types
                 parsedParameters.put("RLC data", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     SNPU("SNPU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ch type";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "SNP layer";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "SNP msg. name";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Protocol subtype";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "SNP msg. length";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 // TODO check type
                 key = "SNP msg.";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     SNPD("SNPD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ch type";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "SNP layer";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "SNP msg. name";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Protocol subtype";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "SNP msg. length";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 // TODO check type
                 key = "SNP msg.";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     GANSU("GANSU") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "GAN msg.";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "GUN subch.";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "GAN msg. length";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 // TODO check type
                 key = "GAN msg. data";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     GANSD("GANSD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "GAN msg.";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "GUN subch.";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "GAN msg. length";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 // TODO check type
                 key = "GAN msg. data";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     L3SM("L3SM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("Type", getIntegerValue(parameters));
                     parsedParameters.put("L3 data", getStringValue(parameters));
                 } else if (2 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("BSIC_s", getStringValue(parameters));
                 } else if (5 == system || 6 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("Channel", getIntegerValue(parameters));
                     parsedParameters.put("SC", getIntegerValue(parameters));
                     parsedParameters.put("L3 data", getStringValue(parameters));
                 } else if (10 == system || 11 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("Channel type", getStringValue(parameters));
                     parsedParameters.put("P_REV", getIntegerValue(parameters));
                     parsedParameters.put("L3 data", getStringValue(parameters));
                 } else if (21 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("L3 data", getStringValue(parameters));
                 } else if (51 == system || 53 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("Channel type", getStringValue(parameters));
                     parsedParameters.put("L3 data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     L2SM("L2SM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("L3 msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("ARFCN", getIntegerValue(parameters));
                     parsedParameters.put("BSIC", getIntegerValue(parameters));
                     parsedParameters.put("Type", getIntegerValue(parameters));
                     parsedParameters.put("L3 data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RRCSM("RRCSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system || 6 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("RRC msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("UARFCN", getIntegerValue(parameters));
                     parsedParameters.put("SC", getIntegerValue(parameters));
                     parsedParameters.put("RRC data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RLCSM("RLCSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("RRC msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("UARFCN", getIntegerValue(parameters));
                     parsedParameters.put("RB", getIntegerValue(parameters));
                     parsedParameters.put("RLC mode", getIntegerValue(parameters));
                     parsedParameters.put("Length indicator", getIntegerValue(parameters));
                     parsedParameters.put("RRC data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     MACSM("MACSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("RLC/MAC msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("Type", getIntegerValue(parameters));
                     parsedParameters.put("RLC/MAC data", getStringValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("MAC msg.", getStringValue(parameters));
                     parsedParameters.put("Frame number", getIntegerValue(parameters));
                     parsedParameters.put("MAC data", getStringValue(parameters));
                     parsedParameters.put("MAC ver", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     LLCSM("LLCSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("LLC msg", getStringValue(parameters));
                     parsedParameters.put("LLC data", getStringValue(parameters));
                 } else if (2 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("LLC msg", getStringValue(parameters));
                     parsedParameters.put("LLC data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SNPSM("SNPSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (12 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("SNP msg. name", getStringValue(parameters));
                     parsedParameters.put("SNP ch type", getStringValue(parameters));
                     parsedParameters.put("SNP layer", getStringValue(parameters));
                     parsedParameters.put("Protocol subtype", getIntegerValue(parameters));
                     parsedParameters.put("SNP data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     RRLPSM("RRLPSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("RRLP msg", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("RRLP data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     GANSM("GANSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("GAN msg.", getStringValue(parameters));
                     parsedParameters.put("Subchannel", getStringValue(parameters));
                     parsedParameters.put("GAN msg. data", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     SIPSM("SIPSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Direction", getIntegerValue(parameters));
                     parsedParameters.put("SIP msg. name", getStringValue(parameters));
                     parsedParameters.put("SIP msg.", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     RTPSM("RTPSM") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 parsedParameters.put("Direction", getIntegerValue(parameters));
                 parsedParameters.put("RTP msg. name", getStringValue(parameters));
                 parsedParameters.put("RTP msg. nr.", getIntegerValue(parameters));
                 parsedParameters.put("RTP message", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     PAA("PAA") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet session context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system || 5 == system || 6 == system) {
                     parsedParameters.put("Initiator", getIntegerValue(parameters));
                     parsedParameters.put("Protocol type", getIntegerValue(parameters));
                     parsedParameters.put("APN", getStringValue(parameters));
                     parsedParameters.put("IP", getStringValue(parameters));
                     parsedParameters.put("Header compr.", getIntegerValue(parameters));
                     parsedParameters.put("Data compr.", getIntegerValue(parameters));
 
                 } else if (11 == system || 12 == system) {
                     parsedParameters.put("Initiator", getIntegerValue(parameters));
                     parsedParameters.put("Protocol type", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Initiator", getIntegerValue(parameters));
                 parsedParameters.put("Protocol type", getIntegerValue(parameters));
                 parsedParameters.put("APN", getStringValue(parameters));// check type
                 parsedParameters.put("IP", getStringValue(parameters));
                 parsedParameters.put("Header compr.", getIntegerValue(parameters));
                 parsedParameters.put("Data compr.", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PAF("PAF") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet session context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Fail status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Deact. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Fail status", getIntegerValue(parameters));
                 parsedParameters.put("Fail time", getIntegerValue(parameters));
                 parsedParameters.put("Fail cause", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PAC("PAC") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet session context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Packet act. state";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "IP";
                 parsedParameters.put(key, getStringValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("PDP act. state", getIntegerValue(parameters));
                 parsedParameters.put("PDP act. time", getIntegerValue(parameters));
                 parsedParameters.put("IP", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     PAD("PAD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet session context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Deact. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Deact. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Deact. time";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Deact. status", getIntegerValue(parameters));
                 parsedParameters.put("Duration", getIntegerValue(parameters));
                 parsedParameters.put("Deact. cause", getStringValue(parameters));
                 parsedParameters.put("Deact. time", getStringValue(parameters));
             }
 
             return parsedParameters;
         }
     },
 
     QSPR("QSPR") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet session context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Avg. TPut class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Peak TPut class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Delay class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Priority class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Reliab. class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min avg. TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min peak TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min delay";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min priority class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min reliability";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. traffic class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. max UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. max DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. gr. UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. gr. DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. deliv. order";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. max SDU size";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. SDU err. ratio";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Req. resid. BER";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Req. deliv. err. SDU";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. transfer delay";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Req. THP";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min traffic class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min max UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min max DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min gr. UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min gr. DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min deliv. order";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min max SDU size";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min SDU err.";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Min resid. BER";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Min del. err. SDU";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min tranfer delay";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Min THP";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Packet Tech.", getIntegerValue(parameters));
                 parsedParameters.put("Avg. TPut class", getIntegerValue(parameters));
                 parsedParameters.put("Peak TPut class", getIntegerValue(parameters));
                 parsedParameters.put("Delay class", getIntegerValue(parameters));
                 parsedParameters.put("Priority class", getIntegerValue(parameters));
                 parsedParameters.put("Reliab. class", getIntegerValue(parameters));
                 parsedParameters.put("Min avg. TPut", getIntegerValue(parameters));
                 parsedParameters.put("Min peak TPut", getIntegerValue(parameters));
                 parsedParameters.put("Min delay", getIntegerValue(parameters));
                 parsedParameters.put("Min priority class", getIntegerValue(parameters));
                 parsedParameters.put("Min reliability", getIntegerValue(parameters));
                 parsedParameters.put("Traffic class", getIntegerValue(parameters));
                 parsedParameters.put("Max UL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Max DL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Gr. UL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Gr. DL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Deliv. order", getIntegerValue(parameters));
                 parsedParameters.put("Max SDU size", getIntegerValue(parameters));
                 parsedParameters.put("SDU err. ratio", getStringValue(parameters));
                 parsedParameters.put("Residual BER", getStringValue(parameters));
                 parsedParameters.put("Deliv. err. PDU", getIntegerValue(parameters));
                 parsedParameters.put("Transfer delay", getIntegerValue(parameters));
                 parsedParameters.put("Traffic prior.", getIntegerValue(parameters));
                 parsedParameters.put("Min tr. class", getIntegerValue(parameters));
                 parsedParameters.put("Min max UL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Min max DL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Min gr. UL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Min gr. DL TPut", getIntegerValue(parameters));
                 parsedParameters.put("Min deliv. ord.", getIntegerValue(parameters));
                 parsedParameters.put("Min max SDU size", getIntegerValue(parameters));
                 parsedParameters.put("Min SDU err.", getStringValue(parameters));
                 parsedParameters.put("Min resid. BER", getStringValue(parameters));
                 parsedParameters.put("Min del err PDU", getIntegerValue(parameters));
                 parsedParameters.put("Min train. delay", getIntegerValue(parameters));
                 parsedParameters.put("Min tr. priority", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     QSPN("QSPN") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Packet session context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Avg. TPut class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Peak TPut class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Delay class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Priority class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Reliab. class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Traffic class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Max UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Max DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Gr. UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Gr. DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Deliv. order";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Max SDU size";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "SDU err. ratio";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Resid. BER";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Deliv. err. SDU";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Transf. delay";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "THP";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 String key = "Packet tech";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Avg. TPut class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Peak TPut class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Delay class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Priority class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Reliab. class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Traffic class";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Max UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Max DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Gr. UL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Gr. DL TPut";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Deliv. order";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Max SDU size";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "SDU err. ratio";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Resid. BER";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Deliv. err. SDU";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Transf. delay";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "THP";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
 
         }
     },
 
     GAA("GAA") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Attach context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 parsedParameters.put("System", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     PCHI("PCHI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("Packet tech.", getIntegerValue(parameters));
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Rac", getIntegerValue(parameters));
                     parsedParameters.put("Radio priority", getIntegerValue(parameters));
                     parsedParameters.put("Priority acc. th.", getIntegerValue(parameters));
                     parsedParameters.put("Split PG cycle", getIntegerValue(parameters));
                     parsedParameters.put("PS coding UL", getIntegerValue(parameters));
                     parsedParameters.put("PS coding DL", getIntegerValue(parameters));
                     parsedParameters.put("#PS TSL UL", getIntegerValue(parameters));
                     parsedParameters.put("#PS TSL DL", getIntegerValue(parameters));
                     parsedParameters.put("PS TNs UL", getIntegerValue(parameters));
                     parsedParameters.put("PS TNs DL", getIntegerValue(parameters));
                     parsedParameters.put("PS TN", getIntegerValue(parameters));
                     parsedParameters.put("PS TN", getIntegerValue(parameters));
                     parsedParameters.put("NW operation", getIntegerValue(parameters));
                     parsedParameters.put("Network crtl. order", getIntegerValue(parameters));
                     parsedParameters.put("IR status UL", getIntegerValue(parameters));
                     parsedParameters.put("PBCCH", getIntegerValue(parameters));
                     parsedParameters.put("CLRS hyst.", getIntegerValue(parameters));
                     parsedParameters.put("CLRS time", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("Packet tech.", getIntegerValue(parameters));
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Rac", getIntegerValue(parameters));
                     parsedParameters.put("NW operation", getIntegerValue(parameters));
                     parsedParameters.put("HSDPA UE categ.", getIntegerValue(parameters));
                     parsedParameters.put("HS-DSCH SC", getIntegerValue(parameters));
                     parsedParameters.put("#HS-SCCH", getIntegerValue(parameters));
                     parsedParameters.put("Pwr. offset", getFloatValue(parameters));
                     parsedParameters.put("ACK/NACK repetitions", getIntegerValue(parameters));
                     parsedParameters.put("H-RNTI", getIntegerValue(parameters));
                     parsedParameters.put("HSUPA UE categ.", getIntegerValue(parameters));
                     parsedParameters.put("TTI", getIntegerValue(parameters));
                     parsedParameters.put("PLnon-max", getIntegerValue(parameters));
                     parsedParameters.put("Rate matching", getIntegerValue(parameters));
                     parsedParameters.put("Primary E-RNTI", getIntegerValue(parameters));
                     parsedParameters.put("Secondary E-RNTI", getIntegerValue(parameters));
                     parsedParameters.put("E-DPCCH power offset", getFloatValue(parameters));
                     parsedParameters.put("Happy bit delay cond.", getIntegerValue(parameters));
                     parsedParameters.put("AGCH OVSF", getIntegerValue(parameters));
                     parsedParameters.put("E-TFCI table", getIntegerValue(parameters));
 
                 } else if (6 == system) {
                     parsedParameters.put("Packet tech.", getIntegerValue(parameters));
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Rac", getIntegerValue(parameters));
                     parsedParameters.put("NW operation", getIntegerValue(parameters));
                 } else if (11 == system) {
                     parsedParameters.put("Packet tech.", getIntegerValue(parameters));
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                 } else if (12 == system) {
                     parsedParameters.put("Packet tech.", getIntegerValue(parameters));
                     parsedParameters.put("Access state", getIntegerValue(parameters));
                     parsedParameters.put("Packet ch type", getIntegerValue(parameters));
                     parsedParameters.put("Packet carrier", getIntegerValue(parameters));
                     parsedParameters.put("Sector ID", getStringValue(parameters));
                     parsedParameters.put("Subnet Mask", getIntegerValue(parameters));
                     parsedParameters.put("CC", getIntegerValue(parameters));
                     parsedParameters.put("Hybrid Mode", getIntegerValue(parameters));
                     parsedParameters.put("Session state", getIntegerValue(parameters));
                     parsedParameters.put("ALMP state", getIntegerValue(parameters));
                     parsedParameters.put("Init state", getIntegerValue(parameters));
                     parsedParameters.put("Idle state", getIntegerValue(parameters));
                     parsedParameters.put("Connected state", getIntegerValue(parameters));
                     parsedParameters.put("Route update state", getIntegerValue(parameters));
                     parsedParameters.put("Overhead msg. state", getIntegerValue(parameters));
 
                 } else if (21 == system) {
                     parsedParameters.put("Packet tech.", getIntegerValue(parameters));
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Rac", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 String key = "Packet tech";
                 final Integer packTech = getIntegerValue(parameters);
                 parsedParameters.put(key, packTech);
                 if (packTech == 1 || packTech == 2) {
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Rac", getIntegerValue(parameters));
                     parsedParameters.put("Radio priority", getIntegerValue(parameters));
                     parsedParameters.put("Priority acc. th.", getIntegerValue(parameters));
                     parsedParameters.put("Split PG cycle", getIntegerValue(parameters));
                     parsedParameters.put("PS coding UL", getIntegerValue(parameters));
                     parsedParameters.put("PS coding DL", getIntegerValue(parameters));
                     parsedParameters.put("#PS TSL UL", getIntegerValue(parameters));
                     parsedParameters.put("#PS TSL DL", getIntegerValue(parameters));
                     parsedParameters.put("PS TNs UL", getIntegerValue(parameters));
                     parsedParameters.put("PS TNs DL", getIntegerValue(parameters));
                     parsedParameters.put("NW operation", getIntegerValue(parameters));
                     parsedParameters.put("Network crtl. order", getIntegerValue(parameters));
                     parsedParameters.put("IR status UL", getIntegerValue(parameters));
                     parsedParameters.put("PBCCH", getIntegerValue(parameters));
                     parsedParameters.put("CLRS hyst.", getIntegerValue(parameters));
                     parsedParameters.put("CLRS time", getIntegerValue(parameters));
                 } else if (packTech == 3 || packTech == 5) {
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Rac", getIntegerValue(parameters));
                     parsedParameters.put("NW mode", getIntegerValue(parameters));
                     parsedParameters.put("HSDPA UE categ.", getIntegerValue(parameters));
                     parsedParameters.put("HS-DSCH SC", getIntegerValue(parameters));
                     parsedParameters.put("#HS-SCCH", getIntegerValue(parameters));
                     parsedParameters.put("Power offset", getFloatValue(parameters));
                     parsedParameters.put("ACK/NACK repetitions", getIntegerValue(parameters));
                     parsedParameters.put("H-RNTI", getIntegerValue(parameters));
                 } else if (packTech == 4) {
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                 } else if (packTech == 6) {
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("Packet ch type", getIntegerValue(parameters));
                     parsedParameters.put("Packet carrier", getIntegerValue(parameters));
                 } else if (packTech == 8) {
                     parsedParameters.put("Packet state", getIntegerValue(parameters));
                     parsedParameters.put("RAC", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     GAF("GAF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Attach context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Attach fail";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Att. fail. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Attach fail", getIntegerValue(parameters));
                 parsedParameters.put("Att. fail time", getIntegerValue(parameters));
                 parsedParameters.put("Att. fail cause", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     GAC("GAC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Attach context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Attach time", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     GAD("GAD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Attach context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Detach status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Detach cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Detach time";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Detach status", getIntegerValue(parameters));
                 parsedParameters.put("Att. duration", getIntegerValue(parameters));
                 parsedParameters.put("Detach cause", getIntegerValue(parameters));
                 parsedParameters.put("Detach time", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     BLER("BLER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("BLER DL", getFloatValue(parameters));
                 parsedParameters.put("System", getIntegerValue(parameters));
                 parsedParameters.put("TrCh #blocks", getIntegerValue(parameters));
                 parsedParameters.put("TrCh #errors", getIntegerValue(parameters));
                 parsedParameters.put("#TrChs", getIntegerValue(parameters));
                 parsedParameters.put("Params/TrCh", getIntegerValue(parameters));
                 parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                 parsedParameters.put("BLER", getFloatValue(parameters));
                 parsedParameters.put("#blocks", getIntegerValue(parameters));
                 parsedParameters.put("#errors", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     RDAS("RDAS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("RLC rate UL", getIntegerValue(parameters));
                 parsedParameters.put("RLC rate DL", getIntegerValue(parameters));
                 parsedParameters.put("RLC retr. UL", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     LDAS("LDAS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("LLC rate UL", getIntegerValue(parameters));
                 parsedParameters.put("LLC rate DL", getIntegerValue(parameters));
                 parsedParameters.put("LLC retrans. UL", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     RLCBLER("RLCBLER") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("BLER", getFloatValue(parameters));
                     parsedParameters.put("#RLC blocks", getIntegerValue(parameters));
                     parsedParameters.put("#RLC errors", getIntegerValue(parameters));
                 } else if (5 == system) {
                     parsedParameters.put("BLER", getFloatValue(parameters));
                     parsedParameters.put("#TRCH blocks", getIntegerValue(parameters));
                     parsedParameters.put("#TRCH errors", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#params/channel", getIntegerValue(parameters));
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("TRCH BLER", getFloatValue(parameters));
                     parsedParameters.put("#TRCH blocks", getIntegerValue(parameters));
                     parsedParameters.put("#TRCH errors", getIntegerValue(parameters));
                 } else if (6 == system) {
                     parsedParameters.put("BLER", getFloatValue(parameters));
                     parsedParameters.put("#TRCH blocks", getIntegerValue(parameters));
                     parsedParameters.put("#TRCH errors", getIntegerValue(parameters));
                     parsedParameters.put("#Chs", getIntegerValue(parameters));
                     parsedParameters.put("#Params", getIntegerValue(parameters));
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("TRCH BLER", getFloatValue(parameters));
                     parsedParameters.put("#TRCH blocks", getIntegerValue(parameters));
                     parsedParameters.put("#TRCH errors", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     RLCRATE("RLCRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("RLC rate UL", getIntegerValue(parameters));
                     parsedParameters.put("RLC rate DL", getIntegerValue(parameters));
                     parsedParameters.put("RLC retr. UL", getFloatValue(parameters));
                 } else if (1 == system) {
                     parsedParameters.put("RLC rate UL", getIntegerValue(parameters));
                     parsedParameters.put("RLC rate DL", getIntegerValue(parameters));
                     parsedParameters.put("RLC retr. UL", getFloatValue(parameters));
                     parsedParameters.put("#RBs", getIntegerValue(parameters));
                     parsedParameters.put("Params/RB", getIntegerValue(parameters));
                     parsedParameters.put("RB ID", getIntegerValue(parameters));
                     parsedParameters.put("RLC rate UL_2", getIntegerValue(parameters));
                     parsedParameters.put("RLC rate DL_2", getIntegerValue(parameters));
                     parsedParameters.put("RLC retr. UL_2", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     LLCRATE("LLCRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("LLC rate UL", getIntegerValue(parameters));
                     parsedParameters.put("LLC rate DL", getIntegerValue(parameters));
                     parsedParameters.put("LLC retrans. UL", getFloatValue(parameters));
                 } else if (21 == system) {
                     parsedParameters.put("LLC rate UL", getIntegerValue(parameters));
                     parsedParameters.put("LLC rate DL", getIntegerValue(parameters));
                     parsedParameters.put("LLC retrans. UL", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     RUA("RUA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Routing area update context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RAU type";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("RAU type", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     RUS("RUS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Routing area update context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Old RAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Old LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Old RAC", getIntegerValue(parameters));
                 parsedParameters.put("Old LAC", getIntegerValue(parameters));
                 parsedParameters.put("RAC", getIntegerValue(parameters));
                 parsedParameters.put("LAC", getIntegerValue(parameters));
                 parsedParameters.put("RAU time", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     RUF("RUF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Routing area update context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Att. RAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Att. LAC";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RAU fail cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Att. RAC", getIntegerValue(parameters));
                 parsedParameters.put("Att. LAC", getIntegerValue(parameters));
                 parsedParameters.put("RAU fail time", getIntegerValue(parameters));
                 parsedParameters.put("RAU fail cause", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     TBFI("TBFI") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#Header params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TLLI";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "#params/TBF";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#UL TBFs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TFI";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RLC win.";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#DL TBFs";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "TFI";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RLC win.";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
 
         }
     },
 
     TBFULE("TBFULE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "UL TBF est. cause";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "UL TBF est. type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "UL TBF est. status";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "#UL TBF est. req";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     MACDAS("MACDAS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 parsedParameters.put("#MAC header params", getIntegerValue(parameters));
                 parsedParameters.put("#TRCH", getIntegerValue(parameters));
                 parsedParameters.put("#params/TRCH", getIntegerValue(parameters));
                 parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                 parsedParameters.put("TRCH type", getIntegerValue(parameters));
                 parsedParameters.put("MAC bit rate DL", getIntegerValue(parameters));
                 parsedParameters.put("MAC block rate DL", getIntegerValue(parameters));
                 parsedParameters.put("HS-DSCH 1st", getFloatValue(parameters));
                 parsedParameters.put("HS-DSCH 2nd", getFloatValue(parameters));
                 parsedParameters.put("HS-DSCH 3rd", getFloatValue(parameters));
             }
             return parsedParameters;
 
         }
     },
     MACRATE("MACRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#TRCH", getIntegerValue(parameters));
                     parsedParameters.put("#params/TRCH", getIntegerValue(parameters));
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("TrCh type", getIntegerValue(parameters));
                     parsedParameters.put("MAC-hs bitrate", getIntegerValue(parameters));
                     parsedParameters.put("MAC-hs block rate", getIntegerValue(parameters));
                     parsedParameters.put("MAC-hs 1st retr.", getFloatValue(parameters));
                     parsedParameters.put("MAC-hs 2nd retr.", getFloatValue(parameters));
                     parsedParameters.put("MAC-hs 3rd retr.", getFloatValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("#MAC header params", getIntegerValue(parameters));
                     parsedParameters.put("MAC rate UL", getIntegerValue(parameters));
                     parsedParameters.put("MAC rate DL", getIntegerValue(parameters));
                     parsedParameters.put("MAC packet rate UL", getIntegerValue(parameters));
                     parsedParameters.put("MAC packet rate DL", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     MACBLER("MACBLER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("#TRCH", getIntegerValue(parameters));
                     parsedParameters.put("#params/TRCH", getIntegerValue(parameters));
                     parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                     parsedParameters.put("TrCh type", getIntegerValue(parameters));
                     parsedParameters.put("#ACK/NACK", getIntegerValue(parameters));
                     parsedParameters.put("MAC-hs BLER DL", getFloatValue(parameters));
                 } else if (25 == system) {
                     parsedParameters.put("PER", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 parsedParameters.put("#MAC header params", getIntegerValue(parameters));
                 parsedParameters.put("#TRCH", getIntegerValue(parameters));
                 parsedParameters.put("#params/TRCH", getIntegerValue(parameters));
                 parsedParameters.put("TrCh ID", getIntegerValue(parameters));
                 parsedParameters.put("TRCH type", getIntegerValue(parameters));
                 parsedParameters.put("#ACK/NACK", getIntegerValue(parameters));
                 parsedParameters.put("BLER", getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     AMRI("AMRI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("AMR init. mode", getIntegerValue(parameters));
                     parsedParameters.put("AMR ICMI", getIntegerValue(parameters));
                     parsedParameters.put("AMR TH1", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS1", getFloatValue(parameters));
                     parsedParameters.put("AMR TH2", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS2", getFloatValue(parameters));
                     parsedParameters.put("AMR TH3", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS3", getFloatValue(parameters));
                     parsedParameters.put("#Active codecs", getIntegerValue(parameters));
                     parsedParameters.put("AMR codecs", getIntegerValue(parameters));
 
                 } else if (21 == system) {
                     parsedParameters.put("AMR init. mode", getIntegerValue(parameters));
                     parsedParameters.put("AMR ICMI", getIntegerValue(parameters));
                     parsedParameters.put("AMR TH1", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS1", getFloatValue(parameters));
                     parsedParameters.put("AMR TH2", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS2", getFloatValue(parameters));
                     parsedParameters.put("AMR TH3", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS3", getFloatValue(parameters));
                     parsedParameters.put("#Active codecs", getIntegerValue(parameters));
                     parsedParameters.put("AMR codecs", getIntegerValue(parameters));
 
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("AMR init. mode", getIntegerValue(parameters));
                     parsedParameters.put("AMR ICMI", getIntegerValue(parameters));
                     parsedParameters.put("AMR TH1", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS1", getFloatValue(parameters));
                     parsedParameters.put("AMR TH2", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS2", getFloatValue(parameters));
                     parsedParameters.put("AMR TH3", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS3", getFloatValue(parameters));
                     parsedParameters.put("#Active codecs", getIntegerValue(parameters));
                     parsedParameters.put("AMR codecs", getIntegerValue(parameters));
                 } else if (system == 32) {
                     parsedParameters.put("AMR init. mode", getIntegerValue(parameters));
                     parsedParameters.put("AMR ICMI", getIntegerValue(parameters));
                     parsedParameters.put("AMR TH1", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS1", getFloatValue(parameters));
                     parsedParameters.put("AMR TH2", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS2", getFloatValue(parameters));
                     parsedParameters.put("AMR TH3", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS3", getFloatValue(parameters));
                     parsedParameters.put("#Active codecs", getIntegerValue(parameters));
                     parsedParameters.put("AMR codecs", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
 
     AMRS("AMRS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (1 == system) {
                     parsedParameters.put("AMR mode UL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode DL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode cmd.", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode req.", getIntegerValue(parameters));
                 } else if (5 == system || 6 == system) {
                     parsedParameters.put("AMR mode UL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode DL", getIntegerValue(parameters));
                 } else if (21 == system) {
                     parsedParameters.put("AMR mode UL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode DL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode cmd.", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode req.", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put("System", system);
                 if (system == 1 || system == 2 || system == 3 || system == 22) {
                     parsedParameters.put("AMR mode UL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode DL", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode cmd.", getIntegerValue(parameters));
                     parsedParameters.put("AMR mode req.", getIntegerValue(parameters));
                 } else if (system == 32) {
                     parsedParameters.put("AMR init. mode", getIntegerValue(parameters));
                     parsedParameters.put("AMR ICMI", getIntegerValue(parameters));
                     parsedParameters.put("AMR TH1", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS1", getFloatValue(parameters));
                     parsedParameters.put("AMR TH2", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS2", getFloatValue(parameters));
                     parsedParameters.put("AMR TH3", getFloatValue(parameters));
                     parsedParameters.put("AMR HYS3", getFloatValue(parameters));
                     parsedParameters.put("#Active codecs", getIntegerValue(parameters));
                     parsedParameters.put("AMR codecs", getIntegerValue(parameters));
                 }
                 // TODO add for UMTS - dont understand how check
             }
             return parsedParameters;
         }
     },
     AMRQ("AMRQ") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "AMR qual. est.";
                 parsedParameters.put(key, getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     AQUL("AQUL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "AQ type UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "AQ MOS";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ sample file";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "AQ ref. file";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "AQ timestamp";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "AQ sample duration UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "AQ activity";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ delay";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ min delay";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ max delay";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ stdev delay";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ SNR";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ insertion gain";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "AQ noise gain";
                 parsedParameters.put(key, getFloatValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("MOS type", getIntegerValue(parameters));
                 parsedParameters.put("AQ mean", getFloatValue(parameters));
                 parsedParameters.put("AQ file", getStringValue(parameters));
                 parsedParameters.put("AQ ref. file", getStringValue(parameters));
                 // timestamp already exist?
                 parsedParameters.put("Timestamp_p", getStringValue(parameters));
                 parsedParameters.put("AQ duration", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     AQDL("AQDL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "AQ type DL";
                 final Integer type = getIntegerValue(parameters);
                 parsedParameters.put(key, type);
                 if (1 == type || 2 == type || 3 == type) {
                     parsedParameters.put("AQ MOS", getFloatValue(parameters));
                     parsedParameters.put("AQ sample", getStringValue(parameters));
                     parsedParameters.put("AQ ref. file", getStringValue(parameters));
                     parsedParameters.put("AQ timestamp", getStringValue(parameters));
                     parsedParameters.put("AQ sample duration", getIntegerValue(parameters));
                     parsedParameters.put("AQ activity", getFloatValue(parameters));
                     parsedParameters.put("AQ delay", getFloatValue(parameters));
                     parsedParameters.put("AQ min delay", getFloatValue(parameters));
                     parsedParameters.put("AQ max delay", getFloatValue(parameters));
                     parsedParameters.put("AQ stdev delay", getFloatValue(parameters));
                     parsedParameters.put("AQ SNR", getFloatValue(parameters));
                     parsedParameters.put("AQ insertion gain", getFloatValue(parameters));
                     parsedParameters.put("AQ noise gain", getFloatValue(parameters));
 
                 } else if (4 == type) {
                     parsedParameters.put("AQ MOS streaming", getFloatValue(parameters));
                 } else if (5 == type) {
                     parsedParameters.put("AQ MOS DL", getFloatValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer aqType = getIntegerValue(parameters);
                 parsedParameters.put("AQ type", aqType);
                 parsedParameters.put("AQ MOS", getFloatValue(parameters));
                 if (aqType == 1 || aqType == 2 || aqType == 3) {
                     parsedParameters.put("AQ sample", getStringValue(parameters));
                     parsedParameters.put("AQ reference", getStringValue(parameters));
                     parsedParameters.put("AQ timestamp", getStringValue(parameters));
                     parsedParameters.put("AQ duration", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     AQI("AQI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "AQ type DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "AQ type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "AQ activity";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "AQ synch.";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("Test type", getIntegerValue(parameters));
                 parsedParameters.put("Activity status", getIntegerValue(parameters));
                 parsedParameters.put("AQ synch.", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     VQDL("VQDL") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Data transfer context ID";
                 List<String> contextName = new ArrayList<String>(1);
                 contextName.add(key);
                 parsedParameters.put(FIRST_CONTEXT_NAME, contextName);
                 key = "VQ type";
                 final Integer vqType = getIntegerValue(parameters);
                 parsedParameters.put(key, vqType);
                 if (1 == vqType) {
                     parsedParameters.put("VQ MOS", getFloatValue(parameters));
                     parsedParameters.put("VQ blockiness", getFloatValue(parameters));
                     parsedParameters.put("VQ blurriness", getFloatValue(parameters));
                     parsedParameters.put("VQ jerkiness", getFloatValue(parameters));
 
                 } else if (2 == vqType) {
                     parsedParameters.put("VQ MOS", getFloatValue(parameters));
                     parsedParameters.put("VQ jitter", getIntegerValue(parameters));
                     parsedParameters.put("VQ PER", getFloatValue(parameters));
                 } else if (3 == vqType) {
                     parsedParameters.put("VQ MOS", getFloatValue(parameters));
                     parsedParameters.put("VQ jitter", getIntegerValue(parameters));
                     parsedParameters.put("VQ PER", getFloatValue(parameters));
                     parsedParameters.put("MOS degradation", getFloatValue(parameters));
                     parsedParameters.put("Deg. due PER", getFloatValue(parameters));
                     parsedParameters.put("Deg. due compress", getFloatValue(parameters));
                     parsedParameters.put("Video frame rate", getFloatValue(parameters));
                     parsedParameters.put("Video protocol", getStringValue(parameters));
                     parsedParameters.put("Video codec", getStringValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 final Integer vqType = getIntegerValue(parameters);
                 parsedParameters.put("VQ type", vqType);
                 parsedParameters.put("VQ MOS", getFloatValue(parameters));
                 if (vqType == 1) {
                     parsedParameters.put("VQ blockiness", getFloatValue(parameters));
                     parsedParameters.put("VQ blurriness", getFloatValue(parameters));
                     parsedParameters.put("VQ jerkiness", getFloatValue(parameters));
                 } else if (vqType == 2) {
                     parsedParameters.put("VQ jitter", getIntegerValue(parameters));
                     parsedParameters.put("VQ PER", getFloatValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     VDAS("VDAS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 parsedParameters.put("Video protocol", getIntegerValue(parameters));
                 parsedParameters.put("Video rate UL", getIntegerValue(parameters));
                 parsedParameters.put("Video rate DL", getIntegerValue(parameters));
                 parsedParameters.put("Video frame rate UL", getIntegerValue(parameters));
                 parsedParameters.put("Video frame rate DL", getIntegerValue(parameters));
                 parsedParameters.put("Video FER", getFloatValue(parameters));
             }
             return parsedParameters;
         }
     },
     VRATE("VRATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 final Integer system = getIntegerValue(parameters);
                 parsedParameters.put(key, system);
                 if (5 == system) {
                     parsedParameters.put("Video protocol", getIntegerValue(parameters));
                     parsedParameters.put("Video rate UL", getIntegerValue(parameters));
                     parsedParameters.put("Video rate DL", getIntegerValue(parameters));
                     parsedParameters.put("Video frame rate UL", getIntegerValue(parameters));
                     parsedParameters.put("Video frame rate DL", getIntegerValue(parameters));
                     parsedParameters.put("Video FER", getFloatValue(parameters));
                     parsedParameters.put("VQI", getFloatValue(parameters));
 
                 }
             }
             return parsedParameters;
         }
     },
     MSGA("MSGA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Message type";
                 final Integer type = getIntegerValue(parameters);
                 parsedParameters.put(key, type);
                 if (1 == type) {
                     parsedParameters.put("SMS context ID", getStringValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                     parsedParameters.put("SMS number", getStringValue(parameters));
                     parsedParameters.put("SMS ser. center", getStringValue(parameters));
                     parsedParameters.put("SMS coding sch.", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. data", getStringValue(parameters));
                 } else {
                     parsedParameters.put("MMS context ID", getStringValue(parameters));
                     parsedParameters.put("MMS msg. type", getIntegerValue(parameters));
                     parsedParameters.put("MMS ser. center", getStringValue(parameters));
                     parsedParameters.put("MMS tr. protocol", getIntegerValue(parameters));
                     parsedParameters.put("#MMS files", getIntegerValue(parameters));
                     parsedParameters.put("MMS filename", getStringValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 final Integer mesType = getIntegerValue(parameters);
                 parsedParameters.put("Message type", mesType);
                 if (mesType == 1) {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Msg. direction", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                     parsedParameters.put("SMS number", getStringValue(parameters));
                     parsedParameters.put("SMS ser. center", getStringValue(parameters));
                     parsedParameters.put("SMS coding sch.", getIntegerValue(parameters));
                     parsedParameters.put("SMS seq number", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                     // TODO check type
                     parsedParameters.put("SMS msg. data", getStringValue(parameters));
                 } else {
                     parsedParameters.put("#Header params", getIntegerValue(parameters));
                     parsedParameters.put("Msg. direction", getIntegerValue(parameters));
                     parsedParameters.put("MMS msg. type", getIntegerValue(parameters));
                     parsedParameters.put("MMS ser. center", getStringValue(parameters));
                     parsedParameters.put("MMS tr. protocol", getIntegerValue(parameters));
                     parsedParameters.put("MMS seq. number", getIntegerValue(parameters));
                     parsedParameters.put("#MMS files", getIntegerValue(parameters));
                     // TODO check type
                     parsedParameters.put("MMS filename", getStringValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     MSGS("MSGS") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Message type";
                 final Integer type = getIntegerValue(parameters);
                 parsedParameters.put(key, type);
                 if (1 == type) {
                     parsedParameters.put("SMS context ID", getStringValue(parameters));
                     parsedParameters.put("Ref. number", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                 } else {
                     parsedParameters.put("MMS context ID", getStringValue(parameters));
                     parsedParameters.put("MMS msg. ID", getIntegerValue(parameters));
                     parsedParameters.put("MMS msg. type", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 final Integer mesType = getIntegerValue(parameters);
                 parsedParameters.put("Message type", mesType);
                 if (mesType == 1) {
                     parsedParameters.put("Msg. direction", getIntegerValue(parameters));
                     parsedParameters.put("Ref. number", getIntegerValue(parameters));
                     parsedParameters.put("SMS seq number", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                 } else {
                     parsedParameters.put("Msg. direction", getIntegerValue(parameters));
                     parsedParameters.put("MMS msg. ID", getStringValue(parameters));
                     parsedParameters.put("MMS seq number", getIntegerValue(parameters));
                     parsedParameters.put("MMS msg. type", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     MSGF("MSGF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Message type";
                 final Integer type = getIntegerValue(parameters);
                 parsedParameters.put(key, type);
                 if (1 == type) {
                     parsedParameters.put("SMS context ID", getStringValue(parameters));
                     parsedParameters.put("SMS cause", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                 } else {
                     parsedParameters.put("MMS context ID", getStringValue(parameters));
                     parsedParameters.put("MMS cause", getIntegerValue(parameters));
                     parsedParameters.put("MMS msg. type", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 final Integer mesType = getIntegerValue(parameters);
                 parsedParameters.put("Message type", mesType);
                 if (mesType == 1) {
                     parsedParameters.put("Msg. direction", getIntegerValue(parameters));
                     parsedParameters.put("SMS cause", getIntegerValue(parameters));
                     parsedParameters.put("SMS seq number", getIntegerValue(parameters));
                     parsedParameters.put("SMS msg. type", getIntegerValue(parameters));
                 } else {
                     parsedParameters.put("Msg. direction", getIntegerValue(parameters));
                     parsedParameters.put("MMS cause", getIntegerValue(parameters));
                     parsedParameters.put("MMS seq number", getIntegerValue(parameters));
                     parsedParameters.put("MMS msg. type", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     },
     PTTA("PTTA") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "PTT tech.";
                 final Integer ptt = getIntegerValue(parameters);
                 parsedParameters.put(key, ptt);
                 if (1 == ptt) {
                     parsedParameters.put("POC context ID", getStringValue(parameters));
                     parsedParameters.put("POC server", getStringValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("PTT type", getStringValue(parameters));
                 parsedParameters.put("POC server", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     PTTF("PTTF") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "PTT tech.";
                 final Integer ptt = getIntegerValue(parameters);
                 parsedParameters.put(key, ptt);
                 if (1 == ptt) {
                     parsedParameters.put("POC context ID", getStringValue(parameters));
                     parsedParameters.put("Fail. status", getIntegerValue(parameters));
                     parsedParameters.put("Fail. cause", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("PTT type", getStringValue(parameters));
                 parsedParameters.put("Fail status", getIntegerValue(parameters));
                 parsedParameters.put("Fail time", getIntegerValue(parameters));
                 parsedParameters.put("Fail cause", getIntegerValue(parameters));
             }
 
             return parsedParameters;
         }
     },
     PTTC("PTTC") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "PTT tech.";
                 final Integer ptt = getIntegerValue(parameters);
                 parsedParameters.put(key, ptt);
                 if (1 == ptt) {
                     parsedParameters.put("POC context ID", getStringValue(parameters));
                     parsedParameters.put("Login time", getIntegerValue(parameters));
                     parsedParameters.put("Group attach time", getIntegerValue(parameters));
                     parsedParameters.put("POC server", getStringValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("PTT type", getStringValue(parameters));
                 parsedParameters.put("Act. time", getIntegerValue(parameters));
                 parsedParameters.put("Login time", getIntegerValue(parameters));
                 parsedParameters.put("Group attach time", getIntegerValue(parameters));
                 parsedParameters.put("POC server", getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     PTTD("PTTD") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "PTT tech.";
                 final Integer ptt = getIntegerValue(parameters);
                 parsedParameters.put(key, ptt);
                 if (1 == ptt) {
                     parsedParameters.put("POC context ID", getStringValue(parameters));
                     parsedParameters.put("Deact. status", getIntegerValue(parameters));
                     parsedParameters.put("Deact. cause", getIntegerValue(parameters));
                     parsedParameters.put("Deact. time", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("PTT type", getStringValue(parameters));
                 parsedParameters.put("Deact. status", getIntegerValue(parameters));
                 parsedParameters.put("Duration", getIntegerValue(parameters));
                 parsedParameters.put("Deact. cause", getIntegerValue(parameters));
                 parsedParameters.put("Deact. time", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
 
     PTTI("PTTI") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "System";
 
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "PTT tech.";
                 final Integer pTech = getIntegerValue(parameters);
                 parsedParameters.put(key, pTech);
                 if (pTech == 1) {
                     parsedParameters.put("POC context ID", getStringValue(parameters));
                     parsedParameters.put("PTT state", getIntegerValue(parameters));
                     parsedParameters.put("PTT user identify", getStringValue(parameters));
                     parsedParameters.put("PTT status", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("System", getIntegerValue(parameters));
                 final Integer pTech = getIntegerValue(parameters);
                 parsedParameters.put("PTT tech.", pTech);
                 if (pTech == 1) {
                     parsedParameters.put("PTT state", getIntegerValue(parameters));
                     parsedParameters.put("PTT user identify", getStringValue(parameters));
                     parsedParameters.put("PTT status", getIntegerValue(parameters));
                 } else {
                     parsedParameters.put("PTT state", getIntegerValue(parameters));
                     parsedParameters.put("PTT comm. type", getIntegerValue(parameters));
                     parsedParameters.put("PTT user identity", getStringValue(parameters));
                 }
             }
             return parsedParameters;
 
         }
     },
     RTPJITTER("RTPJITTER") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "RTP jitter type";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RTP jitter DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RTP jitter UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RTP interarr. DL";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "RTP interarr. UL";
                 parsedParameters.put(key, getIntegerValue(parameters));
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("RTP jitter DL", getIntegerValue(parameters));
                 parsedParameters.put("RTP jitter UL", getIntegerValue(parameters));
                 parsedParameters.put("RTP IAT DL", getIntegerValue(parameters));
                 parsedParameters.put("RTP IAT UL", getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     GPS("GPS") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Lon.";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "Lat.";
                 parsedParameters.put(key, getFloatValue(parameters));
                 key = "Height";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Distance";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "GPS fix";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Satellites";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Velocity";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     TNOTE("TNOTE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "TNote";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     VNOTE("VNOTE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("1.86".equals(version)) {
                 String key = "VNote";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     QNOTE("QNOTE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "ID";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Parent ID";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Question";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Answer";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Description";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
 
         }
     },
     QTRIGGER("QTRIGGER") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "Description";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     MARK("MARK") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "Marker seq.#";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Marker#";
                 parsedParameters.put(key, getIntegerValue(parameters));
             }
             return parsedParameters;
         }
     },
     ERR("ERR") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "Error";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     DATE("DATE") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "Date";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     PAUSE("PAUSE") {
 
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             return parsedParameters;
         }
     },
     APP("APP") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version) || "1.86".equals(version)) {
                 String key = "Ext. app. state";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "#Ext. app. launch";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Ext. app. name";
                 parsedParameters.put(key, getStringValue(parameters));
                 key = "Ext. app. params";
                 parsedParameters.put(key, getStringValue(parameters));
             }
             return parsedParameters;
         }
     },
     LOCK("LOCK") {
         @Override
         public Map<String, Object> fill(String version, List<String> params) {
             Iterator<String> parameters = params.iterator();
             Map<String, Object> parsedParameters = new LinkedHashMap<String, Object>();
             if ("2.01".equals(version)) {
                 String key = "#Forcings";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 key = "Lock type";
                 final Integer lockType = getIntegerValue(parameters);
                 parsedParameters.put(key, lockType);
                 key = "#Params";
                 parsedParameters.put(key, getIntegerValue(parameters));
                 if (lockType == null) {
                     return parsedParameters;// TODO debug
                 }
                 if (1 == lockType) {
                     parsedParameters.put("Locked channel", getIntegerValue(parameters));
                     parsedParameters.put("Locked band", getIntegerValue(parameters));
                 } else if (2 == lockType) {
                     parsedParameters.put("Locked scr.", getIntegerValue(parameters));
                     parsedParameters.put("Locked channel", getIntegerValue(parameters));
                     parsedParameters.put("Locked band", getIntegerValue(parameters));
                 } else if (3 == lockType) {
                     parsedParameters.put("Locked system", getIntegerValue(parameters));
                 } else if (4 == lockType) {
                     parsedParameters.put("Band", getIntegerValue(parameters));
                 } else if (5 == lockType) {
                     parsedParameters.put("Cell barring state", getIntegerValue(parameters));
                 }
             } else if ("1.86".equals(version)) {
                 parsedParameters.put("#Locks", getIntegerValue(parameters));
                 final Integer lockType = getIntegerValue(parameters);
                 parsedParameters.put("Lock type", lockType);
                 if (lockType == null) {
                     return parsedParameters;
                 }
                 if (lockType == 1) {
                     parsedParameters.put("#parameters", getIntegerValue(parameters));
                     parsedParameters.put("Ch. lock channel", getIntegerValue(parameters));
                     parsedParameters.put("Ch. lock system", getIntegerValue(parameters));
                 } else if (lockType == 2) {
                     parsedParameters.put("#parameters", getIntegerValue(parameters));
                     parsedParameters.put("Sector lock SCR", getIntegerValue(parameters));
                     parsedParameters.put("Sector lock ch.", getIntegerValue(parameters));
                 } else if (lockType == 3) {
                     parsedParameters.put("#parameters", getIntegerValue(parameters));
                     parsedParameters.put("System lock system", getIntegerValue(parameters));
                 } else if (lockType == 4) {
                     parsedParameters.put("#parameters", getIntegerValue(parameters));
                     parsedParameters.put("Band lock band", getIntegerValue(parameters));
                 } else if (lockType == 5) {
                     parsedParameters.put("#parameters", getIntegerValue(parameters));
                     parsedParameters.put("Cell barring state", getIntegerValue(parameters));
                 } else if (lockType == 6) {
                     parsedParameters.put("#parameters", getIntegerValue(parameters));
                 }
             }
             return parsedParameters;
         }
     };
     public static String FIRST_CONTEXT_NAME = "FIRST_CONTEXT_NAMEFIRST_CONTEXT_NAME";
     private static HashMap<String, NemoEvents> eventsList = new HashMap<String, NemoEvents>();
     static {
         for (NemoEvents event : NemoEvents.values()) {
             eventsList.put(event.eventId, event);
         }
     }
     private final String eventId;
 
     // protected Set<String> statisticKeys;
 
     /**
      * 
      */
     private NemoEvents(String eventId) {
         this.eventId = eventId;
     }
 
     public static NemoEvents getEventById(String id) {
         return eventsList.get(id);
     }
 
     // public abstract Map<String, Object> fill(List<String> parameters);
 
     public abstract Map<String, Object> fill(String version, List<String> parameters);
 
     /**
      * @param parameters2
      * @return
      */
     protected static Integer getIntegerValue(Iterator<String> parameters) {
 
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         String value = parameters.next();
         if (value.isEmpty()) {
             return null;
         }
 
         return Integer.parseInt(value);
     }
 
     /**
      * @param parameters2
      * @return
      */
     protected static String getStringValue(Iterator<String> parameters) {
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         return parameters.next();
     }
 
     /**
      * @param parameters2
      * @return
      */
     protected static Float getFloatValue(Iterator<String> parameters) {
         if (parameters == null || !parameters.hasNext()) {
             return null;
         }
         String value = parameters.next();
         if (value.isEmpty()) {
             return null;
         }
 
         return Float.parseFloat(value);
     }
 }
