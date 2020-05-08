 package com.zenika.cudf.parser;
 
 import com.zenika.cudf.model.Binary;
 import com.zenika.cudf.model.BinaryId;
 import com.zenika.cudf.model.CUDFDescriptor;
 import com.zenika.cudf.model.Preamble;
 import com.zenika.cudf.model.Request;
 import com.zenika.cudf.parser.model.CUDFParsedDescriptor;
 import com.zenika.cudf.parser.model.ParsedBinary;
 import com.zenika.cudf.parser.model.ParsedPreamble;
 import com.zenika.cudf.parser.model.ParsedRequest;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Antoine Rouaze <antoine.rouaze@zenika.com>
  */
 public abstract class AbstractSerializer {
 
     public void serialize(CUDFDescriptor descriptor) throws ParsingException {
         CUDFParsedDescriptor parsedDescriptor = new CUDFParsedDescriptor();
 
         ParsedPreamble parsedPreamble = processPreamble(descriptor);
         Set<ParsedBinary> parsedBinaries = processBinaries(descriptor);
         ParsedRequest parsedRequest = processRequest(descriptor);
 
         parsedDescriptor.setPreamble(parsedPreamble);
         parsedDescriptor.setPackages(parsedBinaries);
         parsedDescriptor.setRequest(parsedRequest);
 
         parseCUDF(parsedDescriptor);
     }
 
     protected abstract void parseCUDF(CUDFParsedDescriptor parsedDescriptor) throws ParsingException;
 
     private ParsedPreamble processPreamble(CUDFDescriptor descriptor) {
         ParsedPreamble parsedPreamble = new ParsedPreamble();
         Preamble preamble = descriptor.getPreamble();
         parsedPreamble.setProperties(preamble.getProperties());
         parsedPreamble.setReqChecksum(preamble.getReqChecksum());
         parsedPreamble.setStatusChecksum(preamble.getStatusChecksum());
         parsedPreamble.setUnivChecksum(preamble.getUnivChecksum());
         return parsedPreamble;
     }
 
     private Set<ParsedBinary> processBinaries(CUDFDescriptor descriptor) {
         Set<ParsedBinary> parsedBinaries = new HashSet<ParsedBinary>();
         Set<Binary> binaries = descriptor.getPackages();
         for (Binary binary : binaries) {
             ParsedBinary parsedBinary = new ParsedBinary();
             parsedBinary.setBinaryId(binary.getBinaryId());
            parsedBinary.setRevision(parsedBinary.getRevision());
            parsedBinary.setType(parsedBinary.getType());
             Set<BinaryId> binaryIds = new HashSet<BinaryId>();
             for (Binary dependency : binary.getDependencies()) {
                 binaryIds.add(dependency.getBinaryId());
             }
             parsedBinary.setDependencies(binaryIds);
             parsedBinaries.add(parsedBinary);
         }
         return parsedBinaries;
     }
 
     private Set<BinaryId> processBinarySet(Set<Binary> binaries) {
         Set<BinaryId> binaryIds = new HashSet<BinaryId>();
         for (Binary binary : binaries) {
             binaryIds.add(binary.getBinaryId());
         }
         return binaryIds;
     }
 
     private ParsedRequest processRequest(CUDFDescriptor descriptor) {
         Request request = descriptor.getRequest();
         ParsedRequest parsedRequest = new ParsedRequest();
         parsedRequest.setInstall(processBinarySet(request.getInstall()));
         parsedRequest.setRemove(processBinarySet(request.getRemove()));
         parsedRequest.setUpdate(processBinarySet(request.getUpdate()));
         return parsedRequest;
     }
 }
