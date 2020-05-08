 /**
  * Copyright (c) 2010 eXtensible Catalog Organization
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the MIT/X11 license. The text of the license can be
  * found at http://www.opensource.org/licenses/mit-license.php.
  */
 
 package org.extensiblecatalog.ncip.v2.service;
 
 import org.apache.log4j.Logger;
 
 public class Version1BibliographicItemIdentifierCode extends BibliographicItemIdentifierCode {
 
     private static final Logger LOG = Logger.getLogger(Version1BibliographicItemIdentifierCode.class);
 
     public static final String VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE
            = "http://www.niso.org/schemas/ncip/v2_02/ncip_v2_02.xsd";
 
     // Book Item Component Identifier. Source: NISO Draft Standard for Trial Use. BICI (Book Item and Component Identifier).
     public static final Version1BibliographicItemIdentifierCode BICI
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "BICI");
     // CODEN. Source: International CODEN Section of Chemical Abstracts Service.
     public static final Version1BibliographicItemIdentifierCode CODEN
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "CODEN");
     // Digital Object Identifier For example: 10.XXXX/1234. Source: ANSI/NISO Z39.84-2000 Syntax for the Digital Object Identifier.
     public static final Version1BibliographicItemIdentifierCode DOI
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "DOI");
     // Alpha-numeric identifier assigned to government publications by a country's designated government agency, possibly a classification number.
     public static final Version1BibliographicItemIdentifierCode GOVERNMENT_PUBLICATION_NUMBER
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "Government Publication Number");
     // International Standard Book Number. Source: ISO 2108.
     public static final Version1BibliographicItemIdentifierCode ISBN
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "ISBN");
     // International Standard Music Number. Source: ISO 10957.
     public static final Version1BibliographicItemIdentifierCode ISMN
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "ISMN");
     // International Standard Recording Code. Source: ISO 3901.
     public static final Version1BibliographicItemIdentifierCode ISRC
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "ISRC");
     // International Standard Serial Number. Source: ISO 3297.
     public static final Version1BibliographicItemIdentifierCode ISSN
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "ISSN");
     // Alpha-numeric identifier assigned by a national bibliographic agency to a bibliographic Item received under national legal deposit laws.
     public static final Version1BibliographicItemIdentifierCode LEGAL_DEPOSIT_NUMBER
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "Legal Deposit Number");
     // Persistent Uniform Resource Locator. Source: http://www.purl.oclc.org.
     public static final Version1BibliographicItemIdentifierCode PURL
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "PURL");
     // Alpha-numeric identifier assigned by a publisher to a technical report.
     public static final Version1BibliographicItemIdentifierCode REPORT_NUMBER
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "Report Number");
     // Serial Item and Contribution Identifier. Source: ANSI/NISO Z39.56-1996.
     public static final Version1BibliographicItemIdentifierCode SICI
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "SICI");
     // Uniform Resource Identifier. Source: IETF RFC2396.
     public static final Version1BibliographicItemIdentifierCode URI
             = new Version1BibliographicItemIdentifierCode(VERSION_1_BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, "URI");
 
     public static void loadAll() {
         LOG.debug("Loading Version1BibliographicItemIdentifierCode.");
         // Do nothing - merely invoking this method forces the creation of the instances defined above.
     }
 
     public Version1BibliographicItemIdentifierCode(String scheme, String value) {
         super(scheme, value);
     }
 }
