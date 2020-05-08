 /*
  * The MIT License
  *
  * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.synopsys.arc.jenkins.plugins.ownership.nodes;
 
 import com.synopsys.arc.jenkins.plugins.ownership.OwnershipDescription;
 import com.synopsys.arc.jenkins.plugins.ownership.util.AbstractOwnershipHelper;
 import hudson.model.Computer;
 import hudson.model.Node;
 import hudson.model.User;
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * Provides ownership helper for {@link Computer}.
  * The class implements a wrapper of {@link NodeOwnerHelper}.
  * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  */
 public class ComputerOwnerHelper extends AbstractOwnershipHelper<Computer> {
 
     static final ComputerOwnerHelper Instance = new ComputerOwnerHelper();
 
     public static ComputerOwnerHelper getInstance() {
         return Instance;
     }
         
     @Override
     public OwnershipDescription getOwnershipDescription(Computer item) {
         Node node = item.getNode();      
         return node != null 
                 ? NodeOwnerHelper.Instance.getOwnershipDescription(node)
                 : OwnershipDescription.DISABLED_DESCR; // No node - no ownership
     }
     
     @Override
     public Collection<User> getPossibleOwners(Computer computer) {
         Node node = computer.getNode();
         return node != null 
                 ? NodeOwnerHelper.Instance.getPossibleOwners(node)
                 : EMPTY_USERS_COLLECTION;
     }  
     
     public static void setOwnership(Computer computer, OwnershipDescription descr) throws IOException {
         Node node = computer.getNode();
         if (node == null) {
             throw new IOException("Cannot set ownership. Probably, the node has been renamed or deleted.");
         }
         
        NodeOwnerHelper.setOwnership(node, descr);
     }
 }
