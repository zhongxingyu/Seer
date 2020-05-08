 /**
  * This document is a part of the source code and related artifacts for BiLab,
  * an open source interactive workbench for computational biologists.
  * 
  * http://computing.ornl.gov/
  * 
  * Copyright © 2011 Oak Ridge National Laboratory
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package bilab;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.openscience.cdk.ChemFile;
 import org.openscience.cdk.ChemModel;
 import org.openscience.cdk.ChemSequence;
 import org.openscience.cdk.SetOfMolecules;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.io.ChemObjectReader;
 import org.openscience.cdk.io.DummyReader;
 import org.openscience.cdk.io.MDLReader;
 import org.openscience.cdk.io.MDLWriter;
 import org.openscience.cdk.io.PDBReader;
 import org.openscience.cdk.io.ReaderFactory;
 
 import scigol.Debug;
 
 // a molecule that isn't a Seq (i.e. not RNA, DNA etc.)
 @Sophistication(Sophistication.Developer)
 public class MoleculeImpl implements molecule, IResourceIOProvider {
 
     private static List<String> supportedResourceTypes;
 
     static {
         // list of supported resource name type (not extensions)
         supportedResourceTypes = new LinkedList<String>();
         supportedResourceTypes.add("pdb");
         supportedResourceTypes.add("pqs");
         supportedResourceTypes.add("mol");
         supportedResourceTypes.add("sdf");
         supportedResourceTypes.add("xyz");
     }
 
     @Summary("create a resource containing data in a supported format from a molecule")
     public static void exportResource(final molecule m,
             final String resourceName, final String resourceType) {
         Debug.Unimplemented();
     }
 
     // helper
     protected static molecule fromResource(final String resName,
             final String molName) {
         final MoleculeImpl m = importResource("molecules/" + resName, "unknown");
         m.set_name(molName);
         return m;
     }
 
     /*
      * @Summary(
      * "check to see if this resource is in a supported format (by name and possibly also content)"
      * ) boolean isSupportedResourceType(String resourceName) { // we don't
      * bother to look into the resource, we just return true if any of the types
      * that could // be represented by the resource's extension are in our
      * supported list
      * 
      * if (resourceName.length() < 4) return false; String ext =
      * resourceName.substring(resourceName.length()-3,resourceName.length());
      * 
      * // just compare extensions java.util.List<String> extTypes =
      * BilabPlugin.getResourcesTypesWithExtension(ext);
      * 
      * // return true if any type is one we handle for(String type : extTypes)
      * if (supportedResourceTypes.contains(type)) return true;
      * 
      * return false; // we handle none of the possible types for the given
      * resource's extension }
      */
 
     public static List<String> getSupportedResourceTypes() {
         return supportedResourceTypes;
     }
 
     // !!! update this to be the static method above and to return a List of
     // molecules if appropriate
 
     @Summary("create a molecule from a resource containing data in a supported format")
     public static MoleculeImpl importResource(final String resourceName,
             final String resourceType) {
         final MoleculeImpl m = new MoleculeImpl(resourceName);
         m.readResource(resourceName, resourceType);
         return m;
     }
 
     protected String _name;
 
     protected boolean structureKnown;
 
     protected org.openscience.cdk.Molecule mol;
 
     protected String associatedResource;
 
     protected scigol.Map _annotation;
 
     // convenient access to common molecules
     public static molecule A, C, G, T, U, Ala, Arg, Asn, Asp, Cys, Gln, Glu,
             Gly, His, Ile, Leu, Lys, Met, Phe, Pro, Ser, Thr, Trp, Tyr, Val,
 
            Water;
 
     static {
 
         A = fromResource("adenine.mol", "Adenine");
         C = fromResource("cytosine.mol", "Cytosine");
         G = fromResource("guanine.mol", "Guanine");
         T = fromResource("thymine.mol", "Thymine");
         U = fromResource("uracil.mol", "Uracil");
 
         Ala = fromResource("ala.pdb", "Alanine");
         Arg = fromResource("arg.pdb", "Arginine");
         Asn = fromResource("asn.pdb", "Asparagine ");
         Asp = fromResource("asp.pdb", "Aspartate ");
         Cys = fromResource("cys.pdb", "Cysteine");
         Gln = fromResource("gln.pdb", "Glutamine");
         Glu = fromResource("glu.pdb", "Glutamate");
         Gly = fromResource("gly.pdb", "Glycine");
         His = fromResource("his.pdb", "Histidine");
         Ile = fromResource("ile.pdb", "Isoleucine");
         Leu = fromResource("leu.pdb", "Leucine");
         Lys = fromResource("lys.pdb", "Lysine");
         Met = fromResource("met.pdb", "Methionine");
         Phe = fromResource("phe.pdb", "Phenylalanine");
         Pro = fromResource("pro.pdb", "Proline");
         Ser = fromResource("ser.pdb", "Serine");
         Thr = fromResource("thr.pdb", "Threonine");
         Trp = fromResource("trp.pdb", "Tryptophan");
         Tyr = fromResource("tyr.pdb", "Tyrosine");
         Val = fromResource("val.pdb", "Valine");
 
         Water = fromResource("water.xyz", "Water");

     }
 
     public MoleculeImpl(final String name) {
         this._name = name;
         mol = null;
         associatedResource = null;
         _annotation = new scigol.Map();
         structureKnown = false;
     }
 
     public scigol.Any get_annotation(final String key) {
         return get_annotations().get_Item(key);
     }
 
     @Override
     public scigol.Map get_annotations() {
         return _annotation;
     }
 
     @Override
     public String get_AssociatedResource() {
         return associatedResource;
     }
 
     @Override
     public String get_DetailText() {
         return _name;
     }
 
     @Override
     public String get_name() {
         return _name;
     }
 
     @Override
     public String get_ShortText() {
         return _name;
     }
 
     @Override
     public boolean get_StructureKnown() {
         return structureKnown;
     }
 
     @Sophistication(Sophistication.Advanced)
     protected void readResource(final String resourceName,
             final String resourceType) {
         try {
             final java.io.InputStreamReader streamReader = new java.io.InputStreamReader(
                     BilabPlugin.findResourceStream(resourceName));
             if (streamReader == null) {
                 throw new BilabException("unable to open resource:"
                         + resourceName);
             }
 
             ChemObjectReader reader = null;
 
             if (resourceType.equals("pdb")) {
                 reader = new PDBReader(streamReader);
             } else if (resourceType.equals("mol") || resourceType.equals("sdf")) {
                 reader = new MDLReader(streamReader);
             } else {
                 reader = new ReaderFactory().createReader(streamReader);
             }
 
             if ((reader == null) || (reader instanceof DummyReader)) {
                 throw new BilabException("unknown molecule file format");
             }
 
             final ChemFile chemFile = (ChemFile) reader.read(new ChemFile());
             if (chemFile == null) {
                 throw new BilabException(
                         "unable to interpt stream as a molecule:"
                                 + resourceName);
             }
 
             // dig the molecule out of the ChemFile
             if (chemFile.getChemSequenceCount() != 1) {
                 if (chemFile.getChemSequenceCount() > 1) {
                     Notify.userWarning(
                             this,
                             "file contains potentially more that one molecule, only the first will be used:"
                                     + resourceName);
                 } else {
                     throw new BilabException("no molecules found in resource: "
                             + resourceName);
                 }
             }
             final ChemSequence chemSeq = chemFile.getChemSequence(0);
 
             if (chemSeq.getChemModelCount() != 1) {
                 if (chemSeq.getChemModelCount() > 1) {
                     Notify.userWarning(
                             this,
                             "file contains potentially more that one molecule, only the first will be used: "
                                     + resourceName);
                 } else {
                     throw new BilabException("no molecules found in resource: "
                             + resourceName);
                 }
             }
 
             final ChemModel chemModel = chemSeq.getChemModel(0);
             final SetOfMolecules molSet = chemModel.getSetOfMolecules();
             if (molSet.getMoleculeCount() != 1) {
                 if (molSet.getMoleculeCount() > 1) {
                     Notify.userWarning(
                             this,
                             "file contains more that one molecule ("
                                     + molSet.getMoleculeCount()
                                     + "), only the first will be used: "
                                     + resourceName);
                 } else {
                     throw new BilabException("no molecules found in file: "
                             + resourceName);
                 }
             }
 
             mol = molSet.getMolecule(0);
 
             structureKnown = true;
             associatedResource = BilabPlugin.findResource(resourceName)
                     .toString();
             _name = mol.getID();
             try {
                 if (_name == null) {
                     _name = new File(new URI(associatedResource)).getName(); // !!!
                                                                              // associatedResource
                                                                              // may
                                                                              // contain
                                                                              // spaces
                                                                              // in
                                                                              // dir
                                                                              // path,
                                                                              // which
                                                                              // need
                                                                              // to
                                                                              // be
                                                                              // encoded
                                                                              // for
                                                                              // a
                                                                              // URL
                 }
             } catch (final Exception e) {
                 _name = "<unknown>";
             }
 
         } catch (final IOException e) {
             throw new BilabException(
                     "unable to locate resource to import as molecule: "
                             + resourceName);
         } catch (final CDKException e) {
             throw new BilabException("unable to import resource "
                     + resourceName + " as molecule due to CDK error: "
                     + e.getMessage(), e);
         }
 
     }
 
     public void set_name(final String value) {
         _name = value;
     }
 
     // depricated??
     @Override
     @Sophistication(Sophistication.Advanced)
     public String ToMDL() {
         if (!structureKnown) {
             throw new BilabException(
                     "can't convert molecule with unknown structure to MDL format");
         }
 
         Debug.Assert(mol != null);
 
         final java.io.StringWriter sw = new java.io.StringWriter();
         final MDLWriter writer = new MDLWriter(sw);
         // !!!writer.write(mol);
         // !!!writer.close();
         sw.flush();
         return sw.getBuffer().toString();
     }
 
     @Override
     public String toString() {
         return _name;
     }
 
 }
