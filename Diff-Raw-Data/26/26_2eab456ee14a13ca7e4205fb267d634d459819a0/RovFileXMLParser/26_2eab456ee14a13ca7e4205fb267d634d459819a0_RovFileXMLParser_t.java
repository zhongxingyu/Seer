 package com.compomics.natter_remake.controllers;
 
 import com.compomics.natter_remake.model.ChargeState;
 import com.compomics.natter_remake.model.Intensity;
 import com.compomics.natter_remake.model.IntensityList;
 import com.compomics.natter_remake.model.Peptide;
 import com.compomics.natter_remake.model.PeptideGroup;
 import com.compomics.natter_remake.model.PeptideMatch;
 import com.compomics.natter_remake.model.PeptidePartner;
 import com.compomics.natter_remake.model.Protein;
 import com.compomics.natter_remake.model.Ratio;
 import com.compomics.natter_remake.model.RovFileData;
 import com.compomics.natter_remake.model.Scan;
 import com.compomics.natter_remake.model.ScanRange;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.XMLEvent;
 
 /**
  *
  * @author Davy
  */
 public class RovFileXMLParser {
 
     private XMLEvent rovFileLine;
     private RovFileData data;
     private Iterator<Attribute> XMLAttributes;
     private Map<Integer, Peptide> queryNumberToPeptide = new HashMap<Integer, Peptide>(500);
     private Map<Integer, PeptideMatch> peptideMatchIdToPeptideMatch = new HashMap<Integer, PeptideMatch>(500);
 
     public RovFileXMLParser(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         data = new RovFileData();
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("counters")) {
                     parseCounters(rovFileLine);
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("header")) {
                     parseHeader(rovFileXMLReader);
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("peptideGrouping")) {
                     data.setPeptideGroups(parsePeptideGroups(rovFileXMLReader));
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("peptideMatch")) {
                     data.addPeptideMatch(parsePeptideMatch(rovFileXMLReader));
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("proteinhit")) {
                     data.addProteinHit(parseProteinHit(rovFileXMLReader));
                 } // else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("peptide"))
             }
         }
     }
 
     private void parseHeader(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
            System.out.println(rovFileLine.toString());
             if (rovFileLine.isStartElement()) {
                 XMLAttributes = rovFileLine.asStartElement().getAttributes();
                 String val = null;
                 //find way to guarantee order of XMLAttributes                
                 while (XMLAttributes.hasNext()) {
                     Attribute attribute = XMLAttributes.next();
                     if (attribute.getName().getLocalPart().equalsIgnoreCase("val")) {
                         val = attribute.getValue();
                     } else if (attribute.getValue().equalsIgnoreCase("CLE")) {
                         if (val != null) {
                             data.setProteaseUsed(val);
                         } else {
                             data.setProteaseUsed(attribute.getValue());
                         }
                     } else if (attribute.getValue().equalsIgnoreCase("DISTILLERVERSION")) {
                         if (val != null) {
                             data.setDistillerVersion(val);
                         } else {
                             data.setDistillerVersion(XMLAttributes.next().getValue());
                         }
                     } else if (attribute.getValue().equalsIgnoreCase("QUANTITATION")) {
                         if (val != null) {
                             data.setQuantitationMethod(val);
                         } else {
                             data.setQuantitationMethod(XMLAttributes.next().getValue());
                         }
                     } else if (attribute.getValue().equalsIgnoreCase("IONSCORECUTOFF")) {
                         if (val != null) {
                             data.setCutOff(Integer.parseInt(val));
                         } else {
                             data.setCutOff(Integer.parseInt(XMLAttributes.next().getValue()));
                         }
                         //todo find experiment with multiple mods
                     } else if (attribute.getValue().equalsIgnoreCase("MODS")) {
                         if (val != null) {
                             data.addMod(val);
                         } else {
                             data.addMod(XMLAttributes.next().getValue());
                         }
                     } else if (attribute.getValue().equalsIgnoreCase("FILENAME")) {
                         if (val != null) {
                             data.setFileName(val);
                         } else {
                             data.setFileName(XMLAttributes.next().getValue());
                         }
                     }
                 }
             }
             if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("header")) {
                     break;
                 }
             }
         }
     }
 
     private void parseCounters(XMLEvent rovFileLine) throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("peptideCount")) {
                 data.setFoundPeptides(Integer.parseInt(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("peptideMatchCount")) {
                 data.setMatchedPeptides(Integer.parseInt(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("proteinHitCount")) {
                 data.setMatchedProteins(Integer.parseInt(attribute.getValue()));
             }
         }
     }
 
     private List<PeptideGroup> parsePeptideGroups(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         List<PeptideGroup> peptideGroups = new ArrayList<PeptideGroup>();
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("hit")) {
                     XMLAttributes = rovFileLine.asStartElement().getAttributes();
                     PeptideGroup peptideGroup = new PeptideGroup();
                     peptideGroup.setGroupNumber(Integer.parseInt(XMLAttributes.next().getValue()));
                     peptideGroup.addPeptides(parsePeptideGroupPeptides(rovFileXMLReader));
                     peptideGroups.add(peptideGroup);
                 }
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("PeptideGrouping")) {
                     break;
                 }
             }
         }
         return peptideGroups;
     }
 
     private List<Peptide> parsePeptideGroupPeptides(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         List<Peptide> parsedPeptides = new ArrayList<Peptide>(20);
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 Peptide peptide = new Peptide();
                 XMLAttributes = rovFileLine.asStartElement().getAttributes();
                 while (XMLAttributes.hasNext()) {
                     Attribute attribute = XMLAttributes.next();
                     if (attribute.getName().getLocalPart().equalsIgnoreCase("q")) {
                         peptide.setPeptideNumber(Integer.parseInt(attribute.getValue()));
                     } else if (attribute.getName().getLocalPart().equalsIgnoreCase("pepStr")) {
                         peptide.setSequence(attribute.getValue());
                     } else if (attribute.getName().getLocalPart().equalsIgnoreCase("comp")) {
                         peptide.setComposition(attribute.getValue());
                     } else if (attribute.getName().getLocalPart().equalsIgnoreCase("status")) {
                         if (attribute.getValue().equalsIgnoreCase("OK")) {
                             peptide.setValid(true);
                             parsedPeptides.add(peptide);
                         }
                     }
                 }
                 if (peptide.isValid()) {
                     queryNumberToPeptide.put(peptide.getPeptideNumber(), peptide);
                 }
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("hit")) {
                     break;
                 }
             }
         }
         return parsedPeptides;
     }
 
     private PeptideMatch parsePeptideMatch(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         PeptideMatch peptideMatch = new PeptideMatch();
         parsePeptidePartner(rovFileXMLReader, peptideMatch);
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("peptideMatch")) {
                     peptideMatch = new PeptideMatch();
                     parsePeptidePartner(rovFileXMLReader, peptideMatch);
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("chargeStateData")) {
                     peptideMatch.addChargeStateData(parseChargestateForPeptideMatch(rovFileXMLReader));
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("originalRatio")) {
                     peptideMatch.addOriginalRatio(parseOriginalRatioForPeptideMatch());
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("hitRatio")) {
                     peptideMatch.addHitRatio(parseRatioForPartners());
                 }
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("peptideMatch")) {
                     peptideMatchIdToPeptideMatch.put(peptideMatch.getMatchId(), peptideMatch);
                     break;
                 }
             }
         }
         return peptideMatch;
     }
 
     private void parsePeptidePartner(XMLEventReader rovFileXMLReader, PeptideMatch peptideMatch) throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("id")) {
                 peptideMatch.setMatchId(Integer.parseInt(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("chargeState")) {
                 peptideMatch.setChargeState(Integer.parseInt(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("displayIntensity")) {
                 peptideMatch.setIntensity(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("pepStr")) {
                 peptideMatch.setPeptideSequence(attribute.getValue());
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("varModsF")) {
                 peptideMatch.setMods(attribute.getValue());
             }
         }
 
         while (rovFileXMLReader.hasNext()) {
             PeptidePartner peptidePartner = null;
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("partner")) {
                     //TODO clean this up
                     peptidePartner = (parsePeptidePartner(rovFileXMLReader));
                     peptideMatch.addPartner(peptidePartner);
                     break;
                 }
             }
         }
     }
 
     private List<Intensity> parseIntensityForPartner(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         IntensityList intensitiesOfPartner = new IntensityList();
         parseXICForPartner(rovFileLine, intensitiesOfPartner);
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 Intensity intensity = new Intensity();
                 XMLAttributes = rovFileLine.asStartElement().getAttributes();
                 while (XMLAttributes.hasNext()) {
                     Attribute XMLAttribute = XMLAttributes.next();
                     if (XMLAttribute.getName().getLocalPart().equalsIgnoreCase("v")) {
                         intensity.setValue(Double.parseDouble((XMLAttribute.getValue())));
                     } else if (XMLAttribute.getName().getLocalPart().equalsIgnoreCase("scanid")) {
                         intensity.setScanid(Integer.parseInt(XMLAttribute.getValue()));
                     } else if (XMLAttribute.getName().getLocalPart().equalsIgnoreCase("rt")) {
                         intensity.setRetentionTime(Double.parseDouble(XMLAttribute.getValue()));
                     }
                 }
                 intensitiesOfPartner.add(intensity);
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("xic")) {
                     break;
                 }
             }
         }
         return intensitiesOfPartner;
     }
 
     private PeptidePartner parsePeptidePartner(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         PeptidePartner partner = new PeptidePartner();
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("partnerIdentified")) {
                 partner.setPartnerFound(attribute.getValue().equalsIgnoreCase("valid"));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("component")) {
                 partner.setComponent(attribute.getValue());
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("peptideString")) {
                 partner.setPeptideSequence(attribute.getValue());
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("mOverZ")) {
                 partner.setMassOverCharge(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("labelFreeVariableModifications")) {
                 partner.setModificationsOnPeptide(null);
             }
         }
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("xic")) {
                     partner.addIntensities(parseIntensityForPartner(rovFileXMLReader));
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("matches")) {
                     parseMatchesForPartner(partner);
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("range")) {
                     partner.addRange(parseRangesForPartner());
                 }
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("partner")) {
                     break;
                 }
             }
         }
         return partner;
     }
 
     private void parseMatchesForPartner(PeptidePartner peptidePartner) throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("query")) {
                 peptidePartner.addPeptidelinkToPartner(queryNumberToPeptide.get(Integer.parseInt(attribute.getValue())));
             } //else if (xmlAttribute.getName().getLocalPart().equalsIgnoreCase("rank")) {
             //dunno if useful
             // }
         }
     }
 
     private ChargeState parseChargestateForPeptideMatch(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         ChargeState chargeState = new ChargeState();
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         while (XMLAttributes.hasNext()) {
             Attribute xmlAttribute = XMLAttributes.next();
             if (xmlAttribute.getName().getLocalPart().equalsIgnoreCase("bucketWidth")) {
                 chargeState.setBucketWidth(Double.parseDouble(xmlAttribute.getValue()));
             } else if (xmlAttribute.getName().getLocalPart().equalsIgnoreCase("matchedRho")) {
                 chargeState.setCorrelation(Double.parseDouble(xmlAttribute.getValue()));
             } else if (xmlAttribute.getName().getLocalPart().equalsIgnoreCase("totalIntensity")) {
                 chargeState.setTotalIntensity(Double.parseDouble(xmlAttribute.getValue()));
             }
         }
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("bucketarea")) {
                     Scan scan = new Scan();
 
                     XMLAttributes = rovFileLine.asStartElement().getAttributes();
                     while (XMLAttributes.hasNext()) {
                         Attribute attribute = XMLAttributes.next();
                         if (attribute.getName().getLocalPart().equalsIgnoreCase("scan")) {
                             scan.setScanNumber(Integer.parseInt(attribute.getValue()));
                         } else if (attribute.getName().getLocalPart().equalsIgnoreCase("area")) {
                             scan.setArea(Double.parseDouble(attribute.getValue()));
                         }
                     }
                     chargeState.addScan(scan);
                 }
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("chargestatedata")) {
                     break;
                 }
             }
         }
         return chargeState;
     }
 
     private Ratio parseOriginalRatioForPeptideMatch() throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         Ratio ratio = new Ratio();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("ratio")) {
                 ratio.setRatio((attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("valid")) {
                 ratio.setValid(attribute.getValue().equalsIgnoreCase("true"));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("value")) {
                 ratio.setValue(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("quality")) {
                 ratio.setQuality(Double.parseDouble(attribute.getValue()));
             }
             //fitexy?
         }
         return ratio;
     }
 
     private Ratio parseRatioForPartners() throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         Ratio ratio = new Ratio();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("ratio")) {
                 ratio.setRatio((attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("valid")) {
                 ratio.setValid(attribute.getValue().equalsIgnoreCase("true"));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("value")) {
                 ratio.setValue(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("quality")) {
                 ratio.setQuality(Double.parseDouble(attribute.getValue()));
             }
         }
         return ratio;
     }
 
     private Protein parseProteinHit(XMLEventReader rovFileXMLReader) throws XMLStreamException {
         Protein protein = new Protein();
         if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("proteinhit")) {
             XMLAttributes = rovFileLine.asStartElement().getAttributes();
             while (XMLAttributes.hasNext()) {
                 Attribute attribute = XMLAttributes.next();
                 if (attribute.getName().getLocalPart().equalsIgnoreCase("accession")) {
                     protein.setAccession(attribute.getValue());
                 } else if (attribute.getName().getLocalPart().equalsIgnoreCase("score")) {
                     protein.setScore(Integer.parseInt(attribute.getValue()));
                 } else if (attribute.getName().getLocalPart().equalsIgnoreCase("mass")) {
                     protein.setMass(Integer.parseInt(attribute.getValue()));
                 }
             }
         }
 
         while (rovFileXMLReader.hasNext()) {
             rovFileLine = rovFileXMLReader.nextEvent();
             if (rovFileLine.isStartElement()) {
                 if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("proteinhit")) {
                     XMLAttributes = rovFileLine.asStartElement().getAttributes();
                     while (XMLAttributes.hasNext()) {
                         Attribute attribute = XMLAttributes.next();
                         if (attribute.getName().getLocalPart().equalsIgnoreCase("accession")) {
                             protein.setAccession(attribute.getValue());
                         } else if (attribute.getName().getLocalPart().equalsIgnoreCase("score")) {
                             protein.setScore(Integer.parseInt(attribute.getValue()));
                         } else if (attribute.getName().getLocalPart().equalsIgnoreCase("mass")) {
                             protein.setMass(Integer.parseInt(attribute.getValue()));
                         }
                     }
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("proteinratio")) {
                     Ratio ratio = new Ratio();
                     XMLAttributes = rovFileLine.asStartElement().getAttributes();
                     while (XMLAttributes.hasNext()) {
                         Attribute attribute = XMLAttributes.next();
                         if (attribute.getName().getLocalPart().equalsIgnoreCase("rationame")) {
                             ratio.setRatio(attribute.getValue());
                         } else if (attribute.getName().getLocalPart().equalsIgnoreCase("ratio")) {
                             ratio.setValue(Double.parseDouble(attribute.getValue()));
                         } else if (attribute.getName().getLocalPart().equalsIgnoreCase("valid")) {
                             ratio.setValid(attribute.getValue().contentEquals("true"));
                         } //else if (attribute.getName().getLocalPart().equalsIgnoreCase("stdev")) {
                         //}
                     }
                     protein.setRatio(ratio);
                 } else if (rovFileLine.asStartElement().getName().getLocalPart().equalsIgnoreCase("reftoPeptideMatch")) {
                     rovFileLine = rovFileXMLReader.nextEvent();
                     protein.addLinkToPeptideMatch(peptideMatchIdToPeptideMatch.get(Integer.parseInt(rovFileLine.asCharacters().getData())));
                 }
             } else if (rovFileLine.isEndElement()) {
                 if (rovFileLine.asEndElement().getName().getLocalPart().equalsIgnoreCase("proteinhit")) {
                     break;
                 }
             }
         }
 
         return protein;
     }
 
     private void parseXICForPartner(XMLEvent rovFileLine, IntensityList intensitiesForPartnter) throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("peakState")) {
                 intensitiesForPartnter.setValid(attribute.getValue().equalsIgnoreCase("ok"));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("XICPeakStart")) {
                 intensitiesForPartnter.setPeakStart(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("XICPeakEnd")) {
                 intensitiesForPartnter.setPeakEnd(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("XICRegionStart")) {
                 intensitiesForPartnter.setPeakRegionStart(Double.parseDouble(attribute.getValue()));
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase("XICRegionEnd")) {
                 intensitiesForPartnter.setPeakRegionEnd(Double.parseDouble(attribute.getValue()));
             }
         }
     }
 
     private ScanRange parseRangesForPartner() throws XMLStreamException {
         XMLAttributes = rovFileLine.asStartElement().getAttributes();
         ScanRange scanRange = new ScanRange();
         while (XMLAttributes.hasNext()) {
             Attribute attribute = XMLAttributes.next();
             if (attribute.getName().getLocalPart().equalsIgnoreCase("rt")) {
                 if (attribute.getValue().contains("-")) {
                     String[] retentionTimeRange = attribute.getValue().split("-");
                     scanRange.setRetentionTime((Double.parseDouble(retentionTimeRange[0]) + Double.parseDouble(retentionTimeRange[1])) / 2);
                 } else {
                     scanRange.setRetentionTime(Double.parseDouble(attribute.getValue()));
                 }
             } else if (attribute.getName().getLocalPart().equalsIgnoreCase(("scan"))) {
                 if (attribute.getValue().contains("-")) {
                     String[] scanIdRange = attribute.getValue().split("-");
                     scanRange.setScan((Double.parseDouble(scanIdRange[0]) + Double.parseDouble(scanIdRange[1])) / 2);
                 } else {
                     scanRange.setScan(Double.parseDouble(attribute.getValue()));
                 }
             }
         }
         return scanRange;
     }
 
     RovFileData getRovFileData() {
         return data;
     }
 }
