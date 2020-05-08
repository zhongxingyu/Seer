 /*
  * Created on Mar 14, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.security.dao;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module (CSM) Software License, Version 3.0 Copyright
  *2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'CSM Software').  The CSM Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.    
  *
  *This CSM Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.  
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the CSM Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the CSM Software; (ii) distribute and have distributed to
  *and by third parties the CSM Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 
 /**
  * @author kumarvi
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class Queries {
 
 	protected static String getQueryForUserAndGroupForAttribute(String loginName,
 			                                 String objectId,
 											 String attribute,
 											 String privilegeName,
 											 String application_id){
 		 
 		  StringBuffer stbr = new StringBuffer();
 		  stbr.append("and pe.object_id='").append(objectId).append("'");
 		  stbr.append(" and pe.attribute='").append(attribute).append("'");
 		  stbr.append(" and u.login_name='").append(loginName).append("'");
 		  stbr.append(" and p.privilege_name='").append(privilegeName).append("'");
 		  stbr.append(" and pg.application_id=").append(application_id);
 		  stbr.append(" and pe.application_id=").append(application_id);
 		  
 		  StringBuffer sqlBfr = new StringBuffer();
 		  sqlBfr.append(getStaticStringForUserAndGroupForAttribute());
 		  sqlBfr.append(stbr.toString());
 		  sqlBfr.append(" union ");
 		  sqlBfr.append(getStaticStringForUserAndGroupForAttribute2());
 		  sqlBfr.append(stbr.toString());
 		  
 		  return sqlBfr.toString();
 		
 	}
 	protected static String getQueryForCheckPermissionForUserAndGroup(String loginName,
 															            String objectId,
 																		 String privilegeName,
 																		 String application_id){
 
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("and pe.object_id='").append(objectId).append("'");
 		stbr.append(" and u.login_name='").append(loginName).append("'");
 		stbr.append(" and p.privilege_name='").append(privilegeName).append("'");
 		stbr.append(" and pg.application_id=").append(application_id);
 		stbr.append(" and pe.application_id=").append(application_id);
 		
 		StringBuffer sqlBfr = new StringBuffer();
 		sqlBfr.append(getStaticStringForUserAndGroupForAttribute());
 		sqlBfr.append(stbr.toString());
 		sqlBfr.append(" union ");
 		sqlBfr.append(getStaticStringForUserAndGroupForAttribute2());
 		sqlBfr.append(stbr.toString());
 		
 		return sqlBfr.toString();
 		
 		}
 	
 	
 	protected static String getQueryForCheckPermissionForGroup(String userName,
 			                                 String objectId,
 											 String privilegeName,
 											 String application_id){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select 'X'");
 		stbr.append("from csm_protection_group pg,");
 		stbr.append("csm_protection_element pe,");
 		stbr.append("csm_pg_pe pgpe,");
 		stbr.append("csm_user_group_role_pg ugrpg,");
 		stbr.append("csm_user u,");
 		stbr.append("csm_group g,");
 		stbr.append("csm_user_group ug,");
 		stbr.append("csm_role_privilege rp,");
 		stbr.append("csm_privilege p ");
 		stbr
 				.append("where pgpe.protection_group_id = pg.protection_group_id ");
 		stbr
 				.append(" and pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append(" and pe.object_id='" + objectId + "'");
 		stbr
 				.append(" and pg.protection_group_id = ugrpg.protection_group_id ");
 		stbr.append(" and ugrpg.group_id = g.group_id ");
 		stbr.append(" and ug.user_id = u.user_id");
 		stbr.append(" and u.login_name='" + userName + "'");
 		stbr.append(" and ugrpg.role_id = rp.role_id ");
 		stbr.append(" and rp.privilege_id = p.privilege_id");
 		stbr.append(" and p.privilege_name='" + privilegeName + "'");
 		stbr.append(" and pg.application_id="+application_id);
 		stbr.append(" and pe.application_id="+application_id);
 		
 		return stbr.toString();
 	}
 	
 	protected static String getQueryForCheckPermissionForUser(String userName,
 			                                                  String objectId,
 															  String privilegeName,
 															  String application_id){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select 'X'");
 		stbr.append("from csm_protection_group pg,");
 		stbr.append("csm_protection_element pe,");
 		stbr.append("csm_pg_pe pgpe,");
 		stbr.append("csm_user_group_role_pg ugrpg,");
 		stbr.append("csm_user u,");
 		stbr.append("csm_role_privilege rp,");
 		stbr.append("csm_privilege p ");
 		stbr
 				.append("where pgpe.protection_group_id = pg.protection_group_id ");
 		stbr
 				.append(" and pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append(" and pe.object_id='" + objectId + "'");
 		stbr
 				.append(" and pg.protection_group_id = ugrpg.protection_group_id ");
 		stbr.append(" and ugrpg.user_id = u.user_id");
 		stbr.append(" and u.login_name='" + userName + "'");
 		stbr.append(" and ugrpg.role_id = rp.role_id ");
 		stbr.append(" and rp.privilege_id = p.privilege_id");
 		stbr.append(" and p.privilege_name='" + privilegeName + "'");
 		stbr.append(" and pg.application_id="+application_id);
 		stbr.append(" and pe.application_id="+application_id);
 		String sql = stbr.toString();
 		
 		return sql;
 	}
 	
 	private static String getStaticStringForUserAndGroupForAttribute(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select 'X'");
 		stbr.append(" from csm_protection_group pg,"); 
 		stbr.append(" csm_protection_element pe,"); 
 		stbr.append(" csm_pg_pe pgpe,"); 
 		stbr.append(" csm_user_group_role_pg ugrpg,"); 
 		stbr.append(" csm_user u,"); 
 		stbr.append(" csm_role_privilege rp,"); 
 		stbr.append(" csm_role r,");
 		stbr.append(" csm_privilege p");  
 		stbr.append(" where ugrpg.role_id = r.role_id and");
 		stbr.append(" ugrpg.user_id = u.user_id and");
 		stbr.append(" ugrpg.protection_group_id  = pg.protection_group_id  and");
 		stbr.append(" pg.protection_group_id = pgpe.protection_group_id and");
 		stbr.append(" pgpe.protection_element_id = pe.protection_element_id and");
 		stbr.append(" r.role_id = rp.role_id and");
 		stbr.append(" rp.privilege_id = p.privilege_id ");
 		
 		return stbr.toString();
 	}
 	private static String getStaticStringForUserAndGroupForAttribute2(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select 'X'");
 		stbr.append(" from csm_protection_group pg,"); 
 		stbr.append(" csm_protection_element pe,"); 
 		stbr.append(" csm_pg_pe pgpe,"); 
 		stbr.append(" csm_user_group_role_pg ugrpg,"); 
 		stbr.append(" csm_user u,");
 		stbr.append(" csm_user_group ug,");
 		stbr.append(" csm_groups g,");
 		stbr.append(" csm_role_privilege rp,"); 
 		stbr.append(" csm_role r,");
 		stbr.append(" csm_privilege p");  
 		stbr.append(" where ugrpg.role_id = r.role_id and");
 		stbr.append(" ugrpg.group_id = g.group_id and");
 		stbr.append(" g.group_id = ug.group_id and");
 		stbr.append(" ug.user_id = u.user_id and");
 		stbr.append(" ugrpg.protection_group_id  = pg.protection_group_id  and");
 		stbr.append(" pg.protection_group_id = pgpe.protection_group_id and");
 		stbr.append(" pgpe.protection_element_id = pe.protection_element_id and");
 		stbr.append(" r.role_id = rp.role_id and");
 		stbr.append(" rp.privilege_id = p.privilege_id ");
 		
 		return stbr.toString();
 	}
 	
 	public static String getQueryForObjectMap(String loginName,String ObjectId,String privilegeName,String application_id){
 		 StringBuffer stbr = new StringBuffer();
 		stbr.append("and pe.object_id='").append(ObjectId).append("'");
 		stbr.append(" and u.login_name='").append(loginName).append("'");
 		stbr.append(" and p.privilege_name='").append(privilegeName).append("'");
 		stbr.append(" and pg.application_id=").append(application_id);
 		stbr.append(" and pe.application_id=").append(application_id);
 		
 		StringBuffer sqlBfr = new StringBuffer();
 		sqlBfr.append(getQueryForObjectMap_user());
 		sqlBfr.append(stbr.toString());
 		sqlBfr.append(" union ");
 		sqlBfr.append(getQueryForObjectMap_group());
 		sqlBfr.append(stbr.toString());
 		
 		return sqlBfr.toString();
 	}
 	
 	private static String getQueryForObjectMap_user(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select pe.attribute ");
 		stbr.append(" from csm_protection_group pg,"); 
 		stbr.append(" csm_protection_element pe,"); 
 		stbr.append(" csm_pg_pe pgpe,"); 
 		stbr.append(" csm_user_group_role_pg ugrpg,"); 
 		stbr.append(" csm_user u,"); 
 		stbr.append(" csm_role_privilege rp,"); 
 		stbr.append(" csm_role r,");
 		stbr.append(" csm_privilege p");  
 		stbr.append(" where ugrpg.role_id = r.role_id and");
 		stbr.append(" ugrpg.user_id = u.user_id and");
 		stbr.append(" ugrpg.protection_group_id  = pg.protection_group_id  and"); 
 		stbr.append(" pg.protection_group_id = pgpe.protection_group_id and");
 		stbr.append(" pgpe.protection_element_id = pe.protection_element_id and");
 		stbr.append(" r.role_id = rp.role_id and");
 		stbr.append(" rp.privilege_id = p.privilege_id ");
 		
 		
 		return stbr.toString();
 	}
 	private static String getQueryForObjectMap_group(){
 		StringBuffer stbr = new StringBuffer();
 		stbr.append("select pe.attribute");
 		stbr.append(" from csm_protection_group pg,"); 
 		stbr.append(" csm_protection_element pe,"); 
 		stbr.append(" csm_pg_pe pgpe,"); 
 		stbr.append(" csm_user_group_role_pg ugrpg,"); 
 		stbr.append(" csm_user u,");
 		stbr.append(" csm_user_group ug,");
 		stbr.append(" csm_group g,");
 		stbr.append(" csm_role_privilege rp,"); 
 		stbr.append(" csm_role r,");
 		stbr.append(" csm_privilege p");  
 		stbr.append(" where ugrpg.role_id = r.role_id and");
 		stbr.append(" ugrpg.group_id = g.group_id and");
 		stbr.append(" g.group_id = ug.group_id and");
 		stbr.append(" ug.user_id = u.user_id and");
 		stbr.append(" ugrpg.protection_group_id  = pg.protection_group_id  and");
 		stbr.append(" pg.protection_group_id = pgpe.protection_group_id and");
 		stbr.append(" pgpe.protection_element_id = pe.protection_element_id and");
 		stbr.append(" r.role_id = rp.role_id and");
 		stbr.append(" rp.privilege_id = p.privilege_id ");
 		
 		return stbr.toString();
 	}
 	
 	
 	protected static String getQueryforUserPEPrivilegeMap(String user_id)
 	{
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.protection_element_id pe_id, p.privilege_id p_id");
 		stbr.append("      FROM  csm_protection_element pe,");
 		stbr.append("            csm_protection_group pg,");
 		stbr.append("            csm_privilege p,");
 		stbr.append("            csm_group g,");
 		stbr.append("            csm_user_group ug,");
 		stbr.append("            csm_user u,");
 		stbr.append("            csm_pg_pe pgpe,");
 		stbr.append("            csm_role r,");
 		stbr.append("            csm_role_privilege rp,");
 		stbr.append("            csm_user_group_role_pg ugrpg");
		stbr.append("      WHERE pgpe.protection_group_id = ANY (SELECT pg1.protection_group_id FROM csm_protection_group pg1 WHERE pg1.parent_protection_group_id = ugrpg.protection_group_id OR pg1.protection_group_id = ugrpg.protection_group_id)");
 		stbr.append("            AND ugrpg.role_id = rp.role_id");
 		stbr.append("            AND rp.privilege_id = p.privilege_id");
 		stbr.append("            AND pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("            AND pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("            AND ug.group_id = ugrpg.group_id");
 		stbr.append("            AND ug.user_id = ").append(user_id);
 		stbr.append(" UNION ALL ");
 		stbr.append("SELECT DISTINCT pe.protection_element_id pe_id, p.privilege_id p_id");
 		stbr.append("      FROM  csm_protection_element pe,");
 		stbr.append("            csm_protection_group pg,");
 		stbr.append("            csm_privilege p,");
 		stbr.append("            csm_user u,");
 		stbr.append("            csm_pg_pe pgpe,");
 		stbr.append("            csm_role r,");
 		stbr.append("            csm_role_privilege rp,");
 		stbr.append("            csm_user_group_role_pg ugrpg");
		stbr.append("      WHERE pgpe.protection_group_id = ANY (SELECT pg1.protection_group_id FROM csm_protection_group pg1 WHERE pg1.parent_protection_group_id = ugrpg.protection_group_id OR pg1.protection_group_id = ugrpg.protection_group_id)");
 		stbr.append("            AND ugrpg.role_id = rp.role_id");
 		stbr.append("            AND rp.privilege_id = p.privilege_id");
 		stbr.append("            AND pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("            AND pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("            AND ugrpg.user_id = ").append(user_id);
 		stbr.append(" UNION ALL ");
 		stbr.append("SELECT DISTINCT upe.protection_element_id pe_id, 0 p_id");
 		stbr.append("      FROM  csm_user_pe upe");
		stbr.append("      WHERE upe.user_id = ").append(user_id);
 		stbr.append(" ORDER BY pe_id, p_id");
 		
 		return stbr.toString();
 		
 	}
 	
 	protected static String getQueryforGroupPEPrivilegeMap(String group_id)
 	{
 		StringBuffer stbr = new StringBuffer();
 
 		stbr.append("SELECT DISTINCT pe.protection_element_id pe_id, p.privilege_id p_id");
 		stbr.append("      FROM  csm_protection_element pe,");
 		stbr.append("            csm_protection_group pg,");
 		stbr.append("            csm_privilege p,");
 		stbr.append("            csm_group g,");
 		stbr.append("            csm_pg_pe pgpe,");
 		stbr.append("            csm_role r,");
 		stbr.append("            csm_role_privilege rp,");
 		stbr.append("            csm_user_group_role_pg ugrpg");
		stbr.append("      WHERE pgpe.protection_group_id = ANY (SELECT pg1.protection_group_id FROM csm_protection_group pg1 WHERE pg1.parent_protection_group_id = ugrpg.protection_group_id OR pg1.protection_group_id = ugrpg.protection_group_id)");
 		stbr.append("            AND ugrpg.role_id = rp.role_id");
 		stbr.append("            AND rp.privilege_id = p.privilege_id");
 		stbr.append("            AND pg.protection_group_id = pgpe.protection_group_id");
 		stbr.append("            AND pgpe.protection_element_id = pe.protection_element_id");
 		stbr.append("            AND ugrpg.group_id = ").append(group_id);
 		stbr.append("      ORDER BY pe_id, p_id");
 		
 		return stbr.toString();
 		
 	}
 	
 }
