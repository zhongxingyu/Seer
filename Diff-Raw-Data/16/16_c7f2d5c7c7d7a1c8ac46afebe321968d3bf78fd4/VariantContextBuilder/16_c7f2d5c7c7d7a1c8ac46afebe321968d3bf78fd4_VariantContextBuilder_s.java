 /*
  * Copyright (c) 2011, The Broad Institute
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.broadinstitute.sting.utils.variantcontext;
 
import com.google.java.contract.Requires;
 import org.broad.tribble.Feature;
 import org.broad.tribble.TribbleException;
 import org.broad.tribble.util.ParsingUtils;
 import org.broadinstitute.sting.utils.codecs.vcf.VCFConstants;
 import org.broadinstitute.sting.utils.codecs.vcf.VCFParser;
 import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
 
 import java.util.*;
 
 /**
  * Builder class for VariantContext
  *
  * Some basic assumptions here:
  *
  * 1 -- data isn't protectively copied.  If you provide an attribute map to
  * the build, and modify it later, the builder will see this and so will any
  * resulting variant contexts.  It's best not to modify collections provided
  * to a builder.
  *
  * 2 -- the system uses the standard builder model, allowing the simple construction idiom:
  *
  *   builder.source("a").genotypes(gc).id("x").make() => VariantContext
  *
  * 3 -- The best way to copy a VariantContext is:
  *
  *   new VariantContextBuilder(vc).make() => a copy of VC
  *
  * 4 -- validation of arguments is done at the during the final make() call, so a
  * VariantContextBuilder can exist in an inconsistent state as long as those issues
  * are resolved before the call to make() is issued.
  *
  * @author depristo
  */
 public class VariantContextBuilder {
     // required fields
     private String source = null;
     private String contig = null;
     private long start = -1;
     private long stop = -1;
     private Collection<Allele> alleles = null;
 
     // optional -> these are set to the appropriate default value
     private String ID = VCFConstants.EMPTY_ID_FIELD;
     private GenotypesContext genotypes = GenotypesContext.NO_GENOTYPES;
     private double negLog10PError = VariantContext.NO_NEG_LOG_10PERROR;
     private Set<String> filters = null;
     private Map<String, Object> attributes = null;
     private boolean attributesCanBeModified = false;
     private Byte referenceBaseForIndel = null;
     private boolean genotypesAreUnparsed = false;
 
     /** enum of what must be validated */
     final private EnumSet<VariantContext.Validation> toValidate = EnumSet.noneOf(VariantContext.Validation.class);
 
     /**
      * Create an empty VariantContextBuilder where all values adopt their default values.  Note that
      * source, chr, start, stop, and alleles must eventually be filled in, or the resulting VariantContext
      * will throw an error.
      */
     public VariantContextBuilder() {}
 
     /**
      * Create an empty VariantContextBuilder where all values adopt their default values, but the bare min.
      * of info (source, chr, start, stop, and alleles) have been provided to start.
      */
     @Requires({"source != null", "contig != null", "start >= 0", "stop >= 0",
             "alleles != null && !alleles.isEmpty()"})
     public VariantContextBuilder(String source, String contig, long start, long stop, Collection<Allele> alleles) {
         this.source = source;
         this.contig = contig;
         this.start = start;
         this.stop = stop;
         this.alleles = alleles;
         toValidate.add(VariantContext.Validation.ALLELES);
     }
 
     /**
      * Returns a new builder based on parent -- the new VC will have all fields initialized
      * to their corresponding values in parent.  This is the best way to create a derived VariantContext
      *
      * @param parent
      */
     public VariantContextBuilder(VariantContext parent) {
         this.alleles = parent.alleles;
         this.attributes = parent.getAttributes();
         this.attributesCanBeModified = false;
         this.contig = parent.contig;
         this.filters = parent.getFiltersMaybeNull();
         this.genotypes = parent.genotypes;
         this.genotypesAreUnparsed = parent.hasAttribute(VariantContext.UNPARSED_GENOTYPE_MAP_KEY);
         this.ID = parent.getID();
         this.negLog10PError = parent.getNegLog10PError();
         this.referenceBaseForIndel = parent.getReferenceBaseForIndel();
         this.source = parent.getSource();
         this.start = parent.getStart();
         this.stop = parent.getEnd();
     }
 
     /**
      * Tells this builder to use this collection of alleles for the resulting VariantContext
      *
      * @param alleles
      * @return this builder
      */
     @Requires({"alleles != null", "!alleles.isEmpty()"})
     public VariantContextBuilder alleles(final Collection<Allele> alleles) {
         this.alleles = alleles;
         toValidate.add(VariantContext.Validation.ALLELES);
         return this;
     }
 
     /**
      * Tells this builder to use this map of attributes alleles for the resulting VariantContext
      *
      * Attributes can be null -> meaning there are no attributes.  After
      * calling this routine the builder assumes it can modify the attributes
      * object here, if subsequent calls are made to set attribute values
      * @param attributes
      */
     public VariantContextBuilder attributes(final Map<String, Object> attributes) {
         this.attributes = attributes;
         this.attributesCanBeModified = true;
         return this;
     }
 
     /**
      * Puts the key -> value mapping into this builder's attributes
      *
      * @param key
      * @param value
      * @return
      */
     @Requires({"key != null"})
     public VariantContextBuilder attribute(final String key, final Object value) {
         if ( ! attributesCanBeModified ) {
             this.attributesCanBeModified = true;
            this.attributes = new HashMap<String, Object>();
         }
         attributes.put(key, value);
         return this;
     }
 
     /**
      * This builder's filters are set to this value
      *
      * filters can be null -> meaning there are no filters
      * @param filters
      */
     public VariantContextBuilder filters(final Set<String> filters) {
         this.filters = filters;
         return this;
     }
 
     /**
      * {@link #filters}
      *
      * @param filters
      * @return
      */
     public VariantContextBuilder filters(final String ... filters) {
         filters(new HashSet<String>(Arrays.asList(filters)));
         return this;
     }
 
     /**
      * Tells this builder that the resulting VariantContext should have PASS filters
      *
      * @return
      */
     public VariantContextBuilder passFilters() {
         return filters(VariantContext.PASSES_FILTERS);
     }
 
     /**
      * Tells this builder that the resulting VariantContext be unfiltered
      *
      * @return
      */
     public VariantContextBuilder unfiltered() {
         this.filters = null;
         return this;
     }
 
     /**
      * Tells this builder that the resulting VariantContext should use this genotypes GenotypeContext
      *
      * Note that genotypes can be null -> meaning there are no genotypes
      *
      * @param genotypes
      */
     public VariantContextBuilder genotypes(final GenotypesContext genotypes) {
         this.genotypes = genotypes;
         if ( genotypes != null )
             toValidate.add(VariantContext.Validation.GENOTYPES);
         return this;
     }
 
     /**
      * Tells this builder that the resulting VariantContext should use a GenotypeContext containing genotypes
      *
      * Note that genotypes can be null -> meaning there are no genotypes
      *
      * @param genotypes
      */
     public VariantContextBuilder genotypes(final Collection<Genotype> genotypes) {
         return genotypes(GenotypesContext.copy(genotypes));
     }
 
     /**
      * Tells this builder that the resulting VariantContext should use a GenotypeContext containing genotypes
      * @param genotypes
      */
     public VariantContextBuilder genotypes(final Genotype ... genotypes) {
         return genotypes(GenotypesContext.copy(Arrays.asList(genotypes)));
     }
 
     /**
      * Tells this builder that the resulting VariantContext should not contain any GenotypeContext
      */
     public VariantContextBuilder noGenotypes() {
         this.genotypes = null;
         return this;
     }
 
     /**
      * ADVANCED! tells us that the genotypes data is stored as an unparsed attribute
      * @return
      */
     public VariantContextBuilder genotypesAreUnparsed() {
         this.genotypesAreUnparsed = true;
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should have ID
      * @param ID
      * @return
      */
     @Requires("ID != null")
     public VariantContextBuilder id(final String ID) {
         this.ID = ID;
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should not have an ID
      * @return
      */
     public VariantContextBuilder noID() {
         return id(VCFConstants.EMPTY_ID_FIELD);
     }
 
     /**
      * Tells us that the resulting VariantContext should have negLog10PError
      * @param negLog10PError
      * @return
      */
    @Requires("negLog10PError <= 0 || negLog10PError == VariantContext.NO_NEG_LOG_10PERROR")
     public VariantContextBuilder negLog10PError(final double negLog10PError) {
         this.negLog10PError = negLog10PError;
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should use this byte for the reference base
      * Null means no refBase is available
      * @param referenceBaseForIndel
      */
     public VariantContextBuilder referenceBaseForIndel(final Byte referenceBaseForIndel) {
         this.referenceBaseForIndel = referenceBaseForIndel;
         toValidate.add(VariantContext.Validation.REF_PADDING);
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should have source field set to source
      * @param source
      * @return
      */
     @Requires("source != null")
     public VariantContextBuilder source(final String source) {
         this.source = source;
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should have the specified location
      * @param contig
      * @param start
      * @param stop
      * @return
      */
     @Requires({"contig != null", "start >= 0", "stop >= 0"})
     public VariantContextBuilder loc(final String contig, final long start, final long stop) {
         this.contig = contig;
         this.start = start;
         this.stop = stop;
         toValidate.add(VariantContext.Validation.ALLELES);
         toValidate.add(VariantContext.Validation.REF_PADDING);
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should have the specified contig chr
      * @param contig
      * @return
      */
     @Requires({"contig != null"})
     public VariantContextBuilder chr(final String contig) {
         this.contig = contig;
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should have the specified contig start
      * @param start
      * @return
      */
     @Requires({"start >= 0"})
     public VariantContextBuilder start(final long start) {
         this.start = start;
         toValidate.add(VariantContext.Validation.ALLELES);
         toValidate.add(VariantContext.Validation.REF_PADDING);
         return this;
     }
 
     /**
      * Tells us that the resulting VariantContext should have the specified contig stop
      * @param stop
      * @return
      */
     @Requires({"stop >= 0"})
     public VariantContextBuilder stop(final long stop) {
         this.stop = stop;
         return this;
     }
 
     /**
      * Takes all of the builder data provided up to this point, and instantiates
      * a freshly allocated VariantContext with all of the builder data.  This
      * VariantContext is validated as appropriate and if not failing QC (and
      * throwing an exception) is returned.
      *
      * Note that this function can be called multiple times to create multiple
      * VariantContexts from the same builder.
      */
     public VariantContext make() {
         return new VariantContext(source, ID, contig, start, stop, alleles,
                 genotypes, negLog10PError, filters, attributes,
                 referenceBaseForIndel, genotypesAreUnparsed, toValidate);
     }
 }
