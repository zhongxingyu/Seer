 package com.github.podd.impl.file;
 
 import java.util.Collection;
 import java.util.Set;
 
import org.openrdf.model.Graph;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 
import com.github.podd.api.file.PoddFileReference;
 import com.github.podd.api.file.PoddSSHFileReference;
 import com.github.podd.api.file.PoddSSHFileReferenceProcessor;
 
 public class SSHFileReferenceProcessorImpl implements PoddSSHFileReferenceProcessor
 {
     
     @Override
     public boolean canHandle(Model rdfStatements)
     {
         // TODO Auto-generated method stub
         return false;
     }
     
     @Override
     public Collection<PoddSSHFileReference> createReferences(Model rdfStatements)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public Set<URI> getTypes()
     {
         // TODO Auto-generated method stub
         return null;
     }
     
 }
