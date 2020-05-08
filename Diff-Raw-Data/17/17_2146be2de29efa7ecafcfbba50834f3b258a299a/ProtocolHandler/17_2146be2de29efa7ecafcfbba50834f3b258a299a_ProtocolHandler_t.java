 /**
  * Copyright Notice. Copyright 2008 ScenPro, Inc (caBIG
  * Participant).caXchange was created with NCI funding and is part of the
  * caBIG initiative. The software subject to this notice and license includes
  * both human readable source code form and machine readable, binary, object
  * code form (the caBIG Software). This caBIG Software License (the
  * License) is between caBIG Participant and You. You (or Your) shall mean
  * a person or an entity, and all other entities that control, are controlled
  * by, or are under common control with the entity. Control for purposes of
  * this definition means (i) the direct or indirect power to cause the direction
  * or management of such entity, whether by contract or otherwise, or (ii)
  * ownership of fifty percent (50%) or more of the outstanding shares, or (iii)
  * beneficial ownership of such entity. License. Provided that You agree to the
  * conditions described below, caBIG Participant grants You a non-exclusive,
  * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and
  * royalty-free right and license in its rights in the caBIG Software,
  * including any copyright or patent rights therein, to (i) use, install,
  * disclose, access, operate, execute, reproduce, copy, modify, translate,
  * market, publicly display, publicly perform, and prepare derivative works of
  * the caBIG Software in any manner and for any purpose, and to have or permit
  * others to do so; (ii) make, have made, use, practice, sell, and offer for
  * sale, import, and/or otherwise dispose of caBIG Software (or portions
  * thereof); (iii) distribute and have distributed to and by third parties the
  * caBIG Software and any modifications and derivative works thereof; and (iv)
  * sublicense the foregoing rights set out in (i), (ii) and (iii) to third
  * parties, including the right to license such rights to further third parties.
  * For sake of clarity, and not by way of limitation, caBIG Participant shall
  * have no right of accounting or right of payment from You or Your sublicensees
  * for the rights granted under this License. This License is granted at no
  * charge to You. Your downloading, copying, modifying, displaying, distributing
  * or use of caBIG Software constitutes acceptance of all of the terms and
  * conditions of this Agreement. If you do not agree to such terms and
  * conditions, you have no right to download, copy, modify, display, distribute
  * or use the caBIG Software. 1. Your redistributions of the source code for
  * the caBIG Software must retain the above copyright notice, this list of
  * conditions and the disclaimer and limitation of liability of Article 6 below.
  * Your redistributions in object code form must reproduce the above copyright
  * notice, this list of conditions and the disclaimer of Article 6 in the
  * documentation and/or other materials provided with the distribution, if any.
  * 2. Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: This product includes software
  * developed by ScenPro, Inc. If You do not include such end-user
  * documentation, You shall include this acknowledgment in the caBIG Software
  * itself, wherever such third-party acknowledgments normally appear. 3. You may
  * not use the names ScenPro, Inc, The National Cancer Institute, NCI,
  * Cancer Bioinformatics Grid or caBIG to endorse or promote products
  * derived from this caBIG Software. This License does not authorize You to use
  * any trademarks, service marks, trade names, logos or product names of either
  * caBIG Participant, NCI or caBIG, except as required to comply with the
  * terms of this License. 4. For sake of clarity, and not by way of limitation,
  * You may incorporate this caBIG Software into Your proprietary programs and
  * into any third party proprietary programs. However, if You incorporate the
  * caBIG Software into third party proprietary programs, You agree that You are
  * solely responsible for obtaining any permission from such third parties
  * required to incorporate the caBIG Software into such third party proprietary
  * programs and for informing Your sublicensees, including without limitation
  * Your end-users, of their obligation to secure any required permissions from
  * such third parties before incorporating the caBIG Software into such third
  * party proprietary software programs. In the event that You fail to obtain
  * such permissions, You agree to indemnify caBIG Participant for any claims
  * against caBIG Participant by such third parties, except to the extent
  * prohibited by law, resulting from Your failure to obtain such permissions. 5.
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the caBIG Software, or any derivative works
  * of the caBIG Software as a whole, provided Your use, reproduction, and
  * distribution of the Work otherwise complies with the conditions stated in
  * this License. 6. THIS caBIG SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED
  * OR IMPLIED WARRANTIES (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE)
  * ARE DISCLAIMED. IN NO EVENT SHALL THE ScenPro, Inc OR ITS AFFILIATES BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS caBIG SOFTWARE, EVEN IF ADVISED OF
  * THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.ctom.ctlab.handler;
 
 import gov.nih.nci.ctom.ctlab.domain.Protocol;
 import gov.nih.nci.ctom.ctlab.persistence.CTLabDAO;
 import gov.nih.nci.ctom.ctlab.persistence.SQLHelper;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import org.apache.log4j.Logger;
 
 /**
  * ProtocolHandler persists Protocol object to the CTODS database
  *
  * @author asharma
  */
 public class ProtocolHandler extends CTLabDAO implements HL7V3MessageHandlerInterface
 {
 
 	// Logging File
 	private static Logger logger = Logger.getLogger("client");
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see gov.nih.nci.ctom.ctlab.handler.HL7V3MessageHandler#persist(java.sql.Connection,
 	 *      gov.nih.nci.ctom.ctlab.domain.Protocol)
 	 */
 	public void persist(Connection con, Protocol protocol) throws Exception
 	{
 
 		logger.debug("Saving Protocol");
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		Long id = null;
 		boolean identifierUpdInd = false;
 
 		if (protocol.getNciIdentifier() == null)
 		{
 			throw new Exception("Invalid Data, Protocol Identifier Cannot Be Null");
 		}
 		try
 		{
 			// Check if the protocol already exists using identifier
 			ps =
 					con
 							.prepareStatement("select p.ID from protocol p, identifier ii where p.ID = ii.PROTOCOL_ID and ii.extension=?");
 			ps.setString(1, protocol.getIdentifier().getExtension());
 			rs = ps.executeQuery();
 			// If it exists get the ID
 			if (rs.next())
 			{
 				id = rs.getLong(1);
 				protocol.setId(id);
 				ps = con.prepareStatement("select ID from IDENTIFIER where PROTOCOL_ID = ?");
 				ps.setLong(1, id);
 				rs = ps.executeQuery();
 
 				if (rs.next())
 				{
 					protocol.getIdentifier().setId(rs.getLong(1));
 				}
 				logger.debug("Protocol existed");
 			}
 			else
 			{
 				//clean up
 				 ps = SQLHelper.closePreparedStatement(ps);
 
 				// Save/update Identifier
 				HL7V3MessageHandlerFactory.getInstance().getHandler("PROTOCOL_IDENTIFIER").persist(
 						con, protocol);
 				if (protocol.getId() == null)
 				{
 					identifierUpdInd = true;
 					// get ID from sequence
 					id = getNextVal(con, "protocol_seq");
 					ps =
 							con
 									.prepareStatement("insert into protocol (ID, NCI_IDENTIFIER, IDENTIFIER_ASSIGNING_AUTHORITY, LONG_TITLE_TEXT, SHORT_TITLE_TEXT, CTOM_INSERT_DATE, SPONSOR_CODE)  values(?,?,?,?,?,?,?)");
 					ps.setLong(1, id);
 					ps.setString(2, String.valueOf(protocol.getNciIdentifier()));
 					ps.setString(3, String.valueOf(protocol.getIdAssigningAuth()));
 					ps.setString(4, String.valueOf(protocol.getLongTxtTitle()));
 					ps.setString(5, String.valueOf(protocol.getShortTxtTitle()));
 					if (protocol.getCtomInsertDt() == null)
 					{
 						ps.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
 					}
 					else
 					{
 						ps.setTimestamp(6, new java.sql.Timestamp(protocol.getCtomInsertDt()
 								.getTime()));
 					}
 					ps.setString(7, String.valueOf(protocol.getSponsorCode()));
 					ps.execute();
 					if (identifierUpdInd && protocol.getIdentifier() != null)
 					{
 						protocol.setId(id);
 						updateIdentifier(con, protocol);
 					}
 				} // update into participant if there was a participant
 				// associated
 				// with identifier.
 				else
 				{
 					ps =
 							con
 									.prepareStatement("update PROTOCOL set NCI_IDENTIFIER = ?, IDENTIFIER_ASSIGNING_AUTHORITY = ?, LONG_TITLE_TEXT = ? where ID = ?");
 					ps.setString(1, String.valueOf(protocol.getNciIdentifier()));
 					ps.setString(2, String.valueOf(protocol.getIdAssigningAuth()));
 					ps.setString(3, String.valueOf(protocol.getLongTxtTitle()));
 					ps.setLong(4, protocol.getId());
 					ps.executeUpdate();
 					con.commit();
 				}
 			}
 		}
 		catch (SQLException se)
 		{
 			logger.error("Error saving the Protocol",se);
 			throw (new Exception(se.getLocalizedMessage()));
 
 		}
 		finally
 		{
 			//clean up
 			rs = SQLHelper.closeResultSet(rs);
 			ps = SQLHelper.closePreparedStatement(ps);
 		}
 		// save protocol status
 		if (protocol.getStatus() != null)
 		{
 			HL7V3MessageHandlerFactory.getInstance().getHandler("PROTOCOL_STATUS").persist(con,
 					protocol);
 		}
 		// save investigator
 		if (protocol.getInvestigator() != null)
 		{
 			HL7V3MessageHandlerFactory.getInstance().getHandler("INVESTIGATOR").persist(con,
 					protocol);
 		}
 		// save healthcaresite
 		if (protocol.getHealthCareSite() != null)
 		{
 			HL7V3MessageHandlerFactory.getInstance().getHandler("HEALTH_CARE_SITE").persist(con,
 					protocol);
 		}
 	}
 
 	/**
 	 * Update.
 	 *
 	 * @param conn the connection
 	 * @param protocol the protocol
 	 */
 	public void update(Connection conn, Protocol protocol) throws Exception
 	{
 		logger.debug("Updating the Protocol");
		String sql = "update PROTOCOL set LONG_TITLE_TEXT = ?, " +
 				                         "SHORT_TITLE_TEXT = ?, " +
 				                         "PHASE_CODE = ?, " +
 				                         "SPONSOR_CODE = ? " +
 				                         //"IDENTIFIER_ASSIGNING_AUTHORITY = ? " + // this is not available in PA StudyProtocol object
 				                   "WHERE ID = ?";
 		try
 		{
 		    PreparedStatement pstmt = conn.prepareStatement(sql);
		    pstmt.setString(1, protocol.getLongTxtTitle());
		    pstmt.setString(2, protocol.getShortTxtTitle());
		    pstmt.setString(3, protocol.getPhaseCode());
		    pstmt.setString(4, protocol.getSponsorCode());
		    //pstmt.setString(5, protocol.getIdAssigningAuth()); // this is not available in PA StudyProtocol object
		    pstmt.setLong(5, protocol.getId());
 		    pstmt.execute();
 		    conn.commit();
 		}
 		catch (SQLException se)
 		{
 			logger.error("Error updating the Protocol",se);
 			throw (new Exception(se.getLocalizedMessage()));
 
 		}
 		
 		// save protocol status
 		if (protocol.getStatus() != null)
 		{
 			HL7V3MessageHandlerFactory.getInstance().getHandler("PROTOCOL_STATUS").persist(conn, protocol);
 		}
 	}
 
 }
