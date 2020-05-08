 /**
 * This document is a part of the source code and related artifacts for BiLab, an open source interactive workbench for 
 * computational biologists.
 *
 * http://computing.ornl.gov/
 *
 * Copyright © 2011 Oak Ridge National Laboratory
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General 
 * Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any 
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, write to 
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * The license is also available at: http://www.gnu.org/copyleft/lgpl.html
 */
 
 package bilab;
 
 import java.util.LinkedList;
 import java.io.*;
 
 import jalview.FormatAdapter;
 import jalview.ScoreSequence;
 import jalview.Sequence;
 
 import scigol.List;
 
 // A list of sequences with alignment information
 public class alignment extends List implements IAnnotated, IResourceIOProvider {
 
   public alignment() {
     alignedSeqs = new Sequence[0];
     seqScores = new ScoreSequence[0];
     annotation = new scigol.Map();
   }
   
   /** {@inheritDoc} */
   public final scigol.Map get_annotations() {
     return annotation;
   }
   
   @Sophistication(Sophistication.Advanced)
   public Sequence[] alignedSeqs;
   
   @Sophistication(Sophistication.Advanced)
   public ScoreSequence[] seqScores;
   
   protected scigol.Map annotation;
   
   private static java.util.List<String> supportedResourceTypes;
   
   static {
     // list of supported resource name type (not extensions)
     supportedResourceTypes = new LinkedList<String>();
     supportedResourceTypes.add("MSF");
     supportedResourceTypes.add("CLUSTALW");
     supportedResourceTypes.add("FASTA");
     supportedResourceTypes.add("BLC");
     supportedResourceTypes.add("PFAM");
   }
     
   public static java.util.List<String> getSupportedResourceTypes() {
     return supportedResourceTypes;
   }
   
   public static Object importResource(final String resourceName, final String resourceType) {
     try {
       scigol.Debug.WL("asked to import " + resourceName + " of type " + resourceType);
 
       InputStreamReader inputStreamReader = new InputStreamReader(BilabPlugin.findResourceStream(resourceName));
       if (inputStreamReader == null) {
 		throw new BilabException("unable to open resource:" + resourceName);
 	}
       BufferedReader buffReader = new BufferedReader(inputStreamReader);
       
       // read the entire alignment into a string first
       StringBuilder inputString = new StringBuilder();
       String line;
       do {
         line = buffReader.readLine();
         if (line != null) {
 			inputString.append(line + "\n");
 		}
       } while (line != null);
       
       inputStreamReader.close();
             
       String jalViewType;
       if (resourceType.equals("CLUSTALW")) {
 		jalViewType = "CLUSTAL";
 	} else if (resourceType.equals("FASTA")) {
 		jalViewType = "FASTA";
 	} else if (resourceType.equals("MSF")) {
 		jalViewType = "MSF";
 	} else if (resourceType.equals("BLC")) {
 		jalViewType = "BLC";
 	} else if (resourceType.equals("PFAM")) {
 		jalViewType = "PFAM";
 	} else {
 		throw new BilabException("unsupported alignment resource type:" + resourceType);
 	}
       
       alignment aln = new alignment();
       
       aln.alignedSeqs = FormatAdapter.read(jalViewType, inputString.toString());
       aln.seqScores = new ScoreSequence[aln.alignedSeqs.length]; 
       for (int i = 0; i < aln.alignedSeqs.length; i++) {
         aln.seqScores[i] = new ScoreSequence(aln.alignedSeqs[i]);
       }
         
       return aln;
       
     } catch (BilabException e) {
       throw e;
     } catch (Exception e) {
       throw new BilabException("unable to locate/import resource as alignment(s): " + resourceName + " - " + e);
     }    
   }
   
   @Summary("create a resource containing data in a supported format from a seq")
   public static void exportResource(final alignment a, final String resourceName, final String resourceType)
   {
     try {
       String jalViewType;
       if (resourceType.equals("CLUSTALW")) {
 		jalViewType = "CLUSTAL";
 	} else if (resourceType.equals("FASTA")) {
 		jalViewType = "FASTA";
 	} else if (resourceType.equals("MSF")) {
 		jalViewType = "MSF";
 	} else if (resourceType.equals("BLC")) {
 		jalViewType = "BLC";
 	} else if (resourceType.equals("PFAM")) {
 		jalViewType = "PFAM";
 	} else {
 		throw new BilabException("unsupported alignment resource type:" + resourceType);
 	}
       
       OutputStream outStream = BilabPlugin.createResourceStream(resourceName);
       OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
       BufferedWriter buffWriter = new BufferedWriter(outStreamWriter);
       
       String outString = FormatAdapter.get(jalViewType, a.alignedSeqs);
       buffWriter.write(outString);
       buffWriter.flush();
       outStream.close();
       
     } catch (Exception e) {
       throw new BilabException("unable to export sequence as resource: " + resourceName);
     }
   }
   
   public final String toString() {
     String s = "alignment(";
     for (int i = 0; i < alignedSeqs.length; i++) {
       s += alignedSeqs[i].name;
       if (i != alignedSeqs.length - 1) {
 		s += ",";
 	}
     }
     s += ")";
     return s;
   }
 }
