 package com.zenika.cudf.parser;
 
 import com.zenika.cudf.model.*;
 import com.zenika.cudf.parser.model.CUDFParsedDescriptor;
 import com.zenika.cudf.parser.model.ParsedBinary;
 import com.zenika.cudf.parser.model.ParsedPreamble;
 import com.zenika.cudf.parser.model.ParsedRequest;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Adrien Lecharpentier <adrien.lecharpentier@zenika.com>
  */
 public abstract class AbstractDeserializer {
 
     public CUDFDescriptor deserialize() throws ParsingException {
         CUDFParsedDescriptor parsedDescriptor = parseCudf();
         CUDFDescriptor cudfDescriptor = new CUDFDescriptor();
 
         Preamble preamble = processPreamble(parsedDescriptor);
         Binaries binaries = processParsedBinaries(parsedDescriptor);
         Request request = processParsedRequest(parsedDescriptor, binaries);
 
         cudfDescriptor.setPreamble(preamble);
         cudfDescriptor.setBinaries(binaries);
         cudfDescriptor.setRequest(request);
 
         return cudfDescriptor;
     }
 
     protected abstract CUDFParsedDescriptor parseCudf() throws ParsingException;
 
     private Preamble processPreamble(CUDFParsedDescriptor parsedDescriptor) {
         ParsedPreamble parsedPreamble = parsedDescriptor.getPreamble();
         Preamble preamble = new Preamble();
         preamble.setProperties(parsedPreamble.getProperties());
         preamble.setUnivChecksum(parsedPreamble.getUnivChecksum());
         preamble.setStatusChecksum(parsedPreamble.getStatusChecksum());
         preamble.setReqChecksum(parsedPreamble.getReqChecksum());
         return preamble;
     }
 
    private Binaries processParsedBinaries(CUDFParsedDescriptor parsedDescriptor) {
         Set<ParsedBinary> parsedBinaries = parsedDescriptor.getPackages();
         Map<BinaryId, ParsedBinary> parsedRevisions = buildParsedRevisions(parsedBinaries);
 
         Binaries binaries = new DefaultBinaries();
         for (ParsedBinary parsedBinary : parsedBinaries) {
             Binary binary = new Binary(parsedBinary.getBinaryId());
             binary.setRevision(parsedBinary.getRevision());
             binary.setInstalled(parsedBinary.isInstalled());
             binary.setType(parsedBinary.getType());
             Set<Binary> dependencies = processParsedBinaryDependencies(parsedBinary, parsedRevisions);
             binary.setDependencies(dependencies);
             binaries.addBinary(binary);
         }
         return binaries;
     }
 
     private Map<BinaryId, ParsedBinary> buildParsedRevisions(Set<ParsedBinary> parsedBinaries) {
         HashMap<BinaryId, ParsedBinary> revisions = new HashMap<BinaryId, ParsedBinary>();
         for (ParsedBinary parsedBinary : parsedBinaries) {
             revisions.put(parsedBinary.getBinaryId(), parsedBinary);
         }
         return revisions;
     }
 
    private Set<Binary> processParsedBinaryDependencies(ParsedBinary parsedBinary, Map<BinaryId, ParsedBinary> parsedRevisions) {
         Set<Binary> dependencies = new HashSet<Binary>();
         for (BinaryId binaryId : parsedBinary.getDependencies()) {
             ParsedBinary parsedDependency = parsedRevisions.get(binaryId);
             Binary dependency = new Binary(parsedDependency.getBinaryId());
             dependency.setRevision(parsedDependency.getRevision());
             dependency.setInstalled(parsedDependency.isInstalled());
             dependency.setType(parsedDependency.getType());
             dependencies.add(dependency);
         }
         return dependencies;
     }
 
     private Request processParsedRequest(CUDFParsedDescriptor parsedDescriptor, Binaries binaries) {
         Request request = new Request();
         ParsedRequest parsedRequest = parsedDescriptor.getRequest();
         Map<BinaryId, Binary> revisions = buildRevisionMap(binaries.getAllBinaries());
         request.setInstall(processBinaryIdSet(revisions, parsedRequest.getInstall()));
         request.setUpdate(processBinaryIdSet(revisions, parsedRequest.getUpdate()));
         request.setRemove(processBinaryIdSet(revisions, parsedRequest.getRemove()));
         return request;
     }
 
     private HashMap<BinaryId, Binary> buildRevisionMap(Set<Binary> binaries) {
         HashMap<BinaryId, Binary> revisions = new HashMap<BinaryId, Binary>();
         for (Binary binary : binaries) {
             revisions.put(binary.getBinaryId(), binary);
         }
         return revisions;
     }
 
     private Set<Binary> processBinaryIdSet(Map<BinaryId, Binary> revisions, Set<BinaryId> parsedInstall) {
         Set<Binary> binaries = new HashSet<Binary>();
         for (BinaryId binaryId : parsedInstall) {
             Binary binary = revisions.get(binaryId);
             binaries.add(binary);
         }
         return binaries;
     }
 }
