 package com.amee.platform.service.v3.item;
 
 import com.amee.base.validation.ValidationSpecification;
 import com.amee.domain.data.DataItem;
 import com.amee.service.data.DataService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 @Service
 @Scope("prototype")
 public class DataItemValidator implements Validator {
 
     // Alpha numerics & underscore.
     private final static String PATH_PATTERN_STRING = "^[a-zA-Z0-9_]*$";
 
     @Autowired
     private DataService dataService;
 
     private ValidationSpecification nameSpec;
     private ValidationSpecification pathSpec;
     private ValidationSpecification wikiDocSpec;
     private ValidationSpecification provenanceSpec;
 
     public DataItemValidator() {
         // name
         nameSpec = new ValidationSpecification();
         nameSpec.setName("name");
         nameSpec.setMinSize(DataItem.NAME_MIN_SIZE);
         nameSpec.setMaxSize(DataItem.NAME_MAX_SIZE);
         // path
         pathSpec = new ValidationSpecification();
         pathSpec.setName("path");
         pathSpec.setMaxSize(DataItem.PATH_MAX_SIZE);
         pathSpec.setFormat(PATH_PATTERN_STRING);
        pathSpec.setAllowEmpty(true);
         // wikiDoc
         wikiDocSpec = new ValidationSpecification();
         wikiDocSpec.setName("wikiDoc");
         wikiDocSpec.setMaxSize(DataItem.WIKI_DOC_MAX_SIZE);
         wikiDocSpec.setAllowEmpty(true);
         // provenance
         provenanceSpec = new ValidationSpecification();
         provenanceSpec.setName("provenance");
         provenanceSpec.setMaxSize(DataItem.PROVENANCE_MAX_SIZE);
         provenanceSpec.setAllowEmpty(true);
     }
 
     public boolean supports(Class clazz) {
         return DataItem.class.isAssignableFrom(clazz);
     }
 
     public void validate(Object o, Errors e) {
         DataItem dataitem = (DataItem) o;
         // name
         nameSpec.validate(dataitem.getName(), e);
         // path
        // TODO: This must be unique amongst peers, if set.
         pathSpec.validate(dataitem.getPath(), e);
         // wikiDoc
         wikiDocSpec.validate(dataitem.getWikiDoc(), e);
         // provenance
         provenanceSpec.validate(dataitem.getProvenance(), e);
     }
 }
