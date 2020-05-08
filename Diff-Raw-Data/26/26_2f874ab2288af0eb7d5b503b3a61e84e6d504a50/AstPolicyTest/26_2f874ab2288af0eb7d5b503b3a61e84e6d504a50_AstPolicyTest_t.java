 /* 
  * Copyright (C) 2010-2012 PhonyTive LLC
  * http://astive.phonytive.com
  *
  * This file is part of Astive Toolkit
  *
  * Astive is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Astive is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Astive.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.phonytive.astive.server.test;
 
import com.phonytive.astive.server.security.AstPolicy;
import com.phonytive.astive.server.security.AstPolicyUtil;
import java.net.SocketPermission;
 import junit.framework.TestCase;
 
 /**
  *
 * @since 1.0.0
  */
 public class AstPolicyTest extends TestCase {
     
     public AstPolicyTest(String testName) {
         super(testName);
    }    
    
    public void testAstPolicy() {
        AstPolicy ast = AstPolicy.getInstance();
        ast.addPermission(new SocketPermission("127.0.0.1:4444", AstPolicy.DEFAULT_ACTION));
        
        SocketPermission sp1 = new SocketPermission("127.0.0.1:4444", AstPolicy.DEFAULT_ACTION);
        SocketPermission sp2 = new SocketPermission("127.0.0.2:4444", AstPolicy.DEFAULT_ACTION);
        
        assert(AstPolicyUtil.hasPermission(sp1) == true);
        assert(AstPolicyUtil.hasPermission(sp2) == false);
     }
 }
