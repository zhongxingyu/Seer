 /* 
  * WhiteCat - A dynamic role injector for agents.
  *
  * This project represents a new implementation of the so called BlackCat,
  * a project I made during my thesis degree. For more information about such project please see:
  * 
  *   G., L. Ferrari, L. Leonardi,
  *   Injecting Roles in Java Agents Through Run-Time Bytecode Manipulation
  *   IBM Systems Journal, Vol. 44, No. 1, pp.185-208, 2005
  *
  * This new approach exploits a completely different implementation, keeping the
  * same idea of BlackCat.
  * 
  *
  * Copyright (C) Luca Ferrari 2008-2010 - cat4hire@users.sourceforge.net
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package whitecat.test;
 
 
 import org.junit.Before;
 
 import whitecat.core.WhiteCat;
 import whitecat.core.exceptions.WCRoleRepositoryException;
 import whitecat.core.role.IRole;
 import whitecat.core.role.IRoleRepository;
 import whitecat.core.role.descriptors.RoleDescriptor;
 import whitecat.example.DatabaseAdministrator;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Luca Ferrari - cat4hire (at) sourceforge.net
  *
  */
 public class RoleRepositoryTest {
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
     }
 
     @Test
     public void testRoleRepository() throws WCRoleRepositoryException{
 	IRoleRepository repository = WhiteCat.getRoleRepository();
 	
 	// the repository cannot be null
 	if( repository == null )
 	    fail("Role repository is null!");
 	
 	
 	// install a new role
 	DatabaseAdministrator role = new DatabaseAdministrator();
 	RoleDescriptor desc = new RoleDescriptor(){
	    public int hashCode(){
		return -1;
	    }
 	    
	    public boolean equals(Object o ){
		return o.hashCode() == this.hashCode();
	    }
 	};
 	
 	boolean result = repository.installRole(desc, role, false);
 	if( result == false || ! repository.getAvailableRoleDescriptors().contains(desc) )
 	    fail("Role not installed or not available in the repository!");
 	
 	// get back the role
 	IRole backRole = repository.findRole(desc);
 	if( ! backRole.equals( role ) )
 	    fail("The role inserted is not the same I get back!");
 	
 	// remove the role
 	result = repository.removeRole(desc);
 	if( result == false ||  repository.getAvailableRoleDescriptors().contains(desc) )
	    fail("Role not removed from the repository! " + result);
 	
 	// search again for the role
 	backRole = repository.findRole(desc);
 	if( backRole != null )
 	    fail("The role is still in the repository!");
 	
     }
 }
