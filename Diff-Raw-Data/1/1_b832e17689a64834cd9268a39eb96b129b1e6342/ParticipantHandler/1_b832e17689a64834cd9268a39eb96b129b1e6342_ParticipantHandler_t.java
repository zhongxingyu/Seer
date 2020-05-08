 /**
  * Copyright Notice. Copyright 2008 ScenPro, Inc ("caBIG(TM)
  * Participant").caXchange was created with NCI funding and is part of the
  * caBIG(TM) initiative. The software subject to this notice and license includes
  * both human readable source code form and machine readable, binary, object
  * code form (the "caBIG(TM) Software"). This caBIG(TM) Software License (the
  * "License") is between caBIG(TM) Participant and You. "You (or "Your") shall mean
  * a person or an entity, and all other entities that control, are controlled
  * by, or are under common control with the entity. "Control" for purposes of
  * this definition means (i) the direct or indirect power to cause the direction
  * or management of such entity, whether by contract or otherwise, or (ii)
  * ownership of fifty percent (50%) or more of the outstanding shares, or (iii)
  * beneficial ownership of such entity. License. Provided that You agree to the
  * conditions described below, caBIG(TM) Participant grants You a non-exclusive,
  * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and
  * royalty-free right and license in its rights in the caBIG(TM) Software,
  * including any copyright or patent rights therein, to (i) use, install,
  * disclose, access, operate, execute, reproduce, copy, modify, translate,
  * market, publicly display, publicly perform, and prepare derivative works of
  * the caBIG(TM) Software in any manner and for any purpose, and to have or permit
  * others to do so; (ii) make, have made, use, practice, sell, and offer for
  * sale, import, and/or otherwise dispose of caBIG(TM) Software (or portions
  * thereof); (iii) distribute and have distributed to and by third parties the
  * caBIG(TM) Software and any modifications and derivative works thereof; and (iv)
  * sublicense the foregoing rights set out in (i), (ii) and (iii) to third
  * parties, including the right to license such rights to further third parties.
  * For sake of clarity, and not by way of limitation, caBIG(TM) Participant shall
  * have no right of accounting or right of payment from You or Your sublicensees
  * for the rights granted under this License. This License is granted at no
  * charge to You. Your downloading, copying, modifying, displaying, distributing
  * or use of caBIG(TM) Software constitutes acceptance of all of the terms and
  * conditions of this Agreement. If you do not agree to such terms and
  * conditions, you have no right to download, copy, modify, display, distribute
  * or use the caBIG(TM) Software. 1. Your redistributions of the source code for
  * the caBIG(TM) Software must retain the above copyright notice, this list of
  * conditions and the disclaimer and limitation of liability of Article 6 below.
  * Your redistributions in object code form must reproduce the above copyright
  * notice, this list of conditions and the disclaimer of Article 6 in the
  * documentation and/or other materials provided with the distribution, if any.
  * 2. Your end-user documentation included with the redistribution, if any, must
  * include the following acknowledgment: "This product includes software
  * developed by ScenPro, Inc." If You do not include such end-user
  * documentation, You shall include this acknowledgment in the caBIG(TM) Software
  * itself, wherever such third-party acknowledgments normally appear. 3. You may
  * not use the names "ScenPro, Inc", "The National Cancer Institute", "NCI",
  * "Cancer Bioinformatics Grid" or "caBIG(TM)" to endorse or promote products
  * derived from this caBIG(TM) Software. This License does not authorize You to use
  * any trademarks, service marks, trade names, logos or product names of either
  * caBIG(TM) Participant, NCI or caBIG(TM), except as required to comply with the
  * terms of this License. 4. For sake of clarity, and not by way of limitation,
  * You may incorporate this caBIG(TM) Software into Your proprietary programs and
  * into any third party proprietary programs. However, if You incorporate the
  * caBIG(TM) Software into third party proprietary programs, You agree that You are
  * solely responsible for obtaining any permission from such third parties
  * required to incorporate the caBIG(TM) Software into such third party proprietary
  * programs and for informing Your sublicensees, including without limitation
  * Your end-users, of their obligation to secure any required permissions from
  * such third parties before incorporating the caBIG(TM) Software into such third
  * party proprietary software programs. In the event that You fail to obtain
  * such permissions, You agree to indemnify caBIG(TM) Participant for any claims
  * against caBIG(TM) Participant by such third parties, except to the extent
  * prohibited by law, resulting from Your failure to obtain such permissions. 5.
  * For sake of clarity, and not by way of limitation, You may add Your own
  * copyright statement to Your modifications and to the derivative works, and
  * You may provide additional or different license terms and conditions in Your
  * sublicenses of modifications of the caBIG(TM) Software, or any derivative works
  * of the caBIG(TM) Software as a whole, provided Your use, reproduction, and
  * distribution of the Work otherwise complies with the conditions stated in
  * this License. 6. THIS caBIG(TM) SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED
  * OR IMPLIED WARRANTIES (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE)
  * ARE DISCLAIMED. IN NO EVENT SHALL THE ScenPro, Inc OR ITS AFFILIATES BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS caBIG(TM) SOFTWARE, EVEN IF ADVISED OF
  * THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.ctom.ctlab.handler;
 
 import gov.nih.nci.ctom.ctlab.domain.Participant;
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
  * ParticipantHandler persists Participant object to the CTODS database
  * <P>
  * 
  * @author asharma
  */
 public class ParticipantHandler extends CTLabDAO implements HL7V3MessageHandlerInterface
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
 
 		logger.debug("Saving the Participant");
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		boolean identifierUpdInd = false;
 
 		// retrieve Participant details from Protocol
 		Participant participant =
 				protocol.getHealthCareSite().getStudyParticipantAssignment().getParticipant();
 
 		// Get the concept descriptor ids for the race and gender
 		Long genderCDId =
 				insertOrsaveConceptDescriptor(con, participant.getAdminGenderCode(), participant
 						.getAdminGenderCodeSystem(), participant.getAdminGenderCodeSystemName(),
 						null, null);
 		Long raceCDId =
 				insertOrsaveConceptDescriptor(con, participant.getRaceCode(), participant
 						.getRaceCodeSystem(), participant.getRaceCodeSystemName(), null, null);
 
 		// updates or inserts the IDentifier
 		HL7V3MessageHandlerFactory.getInstance().getHandler("PARTICIPANT_IDENTIFIER").persist(con,
 				protocol);
 
 		try
 		{
 			// insert into participant if there was no participant associated
 			// with
 			// identifier.
 			if (participant.getId() == null)
 			{
 				identifierUpdInd = true;
 
 				// get the next value of id from sequence
 				Long id = getNextVal(con, "Person_SEQ");
 
 				// set the id
 				participant.setId(id);
 
 				boolean genderCode = false;
 				boolean raceCode = false;
 				if (genderCDId != null)
 				{
 					genderCode = true;
 				}
 				if (raceCDId != null)
 				{
 					raceCode = true;
 				}
 
 				if (raceCode && genderCode)
 				{
 					ps =
 							con
 									.prepareStatement("insert into PARTICIPANT (ID, BIRTH_DATE, INITIALS, LAST_NAME, FIRST_NAME, ETHNIC_GROUP_CODE, CTOM_INSERT_DATE, ADM_GNDR_CONCEPT_DESCRIPTOR_ID,RACE_CONCEPT_DESCRIPTOR_ID )  values(?,?,?,?,?,?,?,?,?)");
 					ps.setLong(8, genderCDId);
 					ps.setLong(9, raceCDId);
 				}
 				else if (!raceCode && !genderCode)
 				{
 					ps =
 							con
 									.prepareStatement("insert into PARTICIPANT (ID, BIRTH_DATE, INITIALS, LAST_NAME, FIRST_NAME, ETHNIC_GROUP_CODE, CTOM_INSERT_DATE)  values(?,?,?,?,?,?,?)");
 
 				}
 				else if (!raceCode)
 				{
 					ps =
 							con
 									.prepareStatement("insert into PARTICIPANT (ID, BIRTH_DATE, INITIALS, LAST_NAME, FIRST_NAME, ETHNIC_GROUP_CODE, CTOM_INSERT_DATE, ADM_GNDR_CONCEPT_DESCRIPTOR_ID )  values(?,?,?,?,?,?,?,?)");
 					ps.setLong(8, genderCDId);
 				}
 				else if (!genderCode)
 				{
 					ps =
 							con
 									.prepareStatement("insert into PARTICIPANT (ID, BIRTH_DATE, INITIALS, LAST_NAME, FIRST_NAME, ETHNIC_GROUP_CODE, CTOM_INSERT_DATE, RACE_CONCEPT_DESCRIPTOR_ID )  values(?,?,?,?,?,?,?,?)");
 					ps.setLong(8, raceCDId);
 				}
 
 				ps.setLong(1, participant.getId());
 				if (participant.getBirthDate() != null)
 				{
 					ps.setDate(2, new java.sql.Date(participant.getBirthDate().getTime()));
 				}
 				else
 				{
 					ps.setDate(2, null);
 				}
 
 				ps.setString(3, participant.getInitials());
 				ps.setString(4, participant.getLastName());
 				ps.setString(5, participant.getFirstName());
 				ps.setString(6, participant.getEthnicGroupCode());
 				if (participant.getCtomInsertDate() == null)
 				{
 					ps.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
 				}
 				else
 				{
 					ps.setTimestamp(7, new java.sql.Timestamp(participant.getCtomInsertDate()
 							.getTime()));
 				}
 				ps.execute();
 				con.commit();
 
 				if (identifierUpdInd && participant.getIdentifier() != null)
 				{
 					updateIdentifier(con, participant);
 				}
 			} // update into participant if there was a participant associated
 			// with identifier.
 			else
 			{
 				ps =
 						con
 								.prepareStatement("update PARTICIPANT set BIRTH_DATE = ?, INITIALS = ?, LAST_NAME = ?, FIRST_NAME = ?, ETHNIC_GROUP_CODE=?, ADM_GNDR_CONCEPT_DESCRIPTOR_ID=?,RACE_CONCEPT_DESCRIPTOR_ID=?,CTOM_UPDATE_DATE=? where ID = ?");
 				if (participant.getBirthDate() != null)
 				{
 					ps.setDate(1, new java.sql.Date(participant.getBirthDate().getTime()));
 				}
 				else
 				{
 					ps.setDate(1, null);
 				}
 				ps.setString(2, participant.getInitials());
 				ps.setString(3, participant.getLastName());
 				ps.setString(4, participant.getFirstName());
 				ps.setString(5, participant.getEthnicGroupCode());
 				ps.setLong(6, genderCDId);
 				ps.setLong(7, raceCDId);
 				if (participant.getCtomInsertDate() == null)
 				{
 					ps.setTimestamp(8, new java.sql.Timestamp(new Date().getTime()));
 				}
 				else
 				{
 					ps.setTimestamp(8, new java.sql.Timestamp(participant.getCtomInsertDate()
 							.getTime()));
 				}
				ps.setLong(9, participant.getId());
 				ps.executeUpdate();
 				con.commit();
 			}
 		}
 		catch (SQLException se)
 		{
 			logger.error("Error saving the Participant",se);
 			throw (new Exception(se.getLocalizedMessage()));
 
 		}
 		finally
 		{
 			//clean up
 			rs = SQLHelper.closeResultSet(rs);
 			ps = SQLHelper.closePreparedStatement(ps);
 
 		}
 
 	}
 
 }
