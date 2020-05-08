 /**
  *  This file is part of MythTV for Android
  * 
  *  MythTV for Android is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  MythTV for Android is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MythTV for Android.  If not, see <http://www.gnu.org/licenses/>.
  *   
  * This software can be found at <https://github.com/MythTV-Android/MythTV-Service-API/>
  *
  */
 package org.mythtv.services.api.test.v026;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mythtv.services.api.ETagInfo;
 import org.mythtv.services.api.MythServiceApiRuntimeException;
 import org.mythtv.services.api.v026.GuideOperations;
 import org.mythtv.services.api.v026.beans.ChannelInfo;
 import org.mythtv.services.api.v026.beans.Program;
 import org.mythtv.services.api.v026.beans.ProgramGuideWrapper;
 import org.mythtv.services.api.v026.beans.ProgramWrapper;
 import org.springframework.http.ResponseEntity;
 
 import java.util.List;
 
 /**
  * @author Sebastien Astie
  * 
  */
 public class GuideOperationsTest extends BaseMythtvServiceApiTester {
 
 	private int iconsize;
 	private int chanid = 2502;
 	private DateTime now;
 	private DateTime tomorrow;
 
 	private GuideOperations operations;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.mythtv.services.api.test.BaseMythtvServiceApiTester#setUp()
 	 */
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		now = new DateTime();
 		tomorrow = now.plus( Period.days( 1 ) );
 		operations = api.guideOperations();
 		iconsize = Integer.parseInt( properties.getProperty(
 				"MythServicesServiceProvider.GuideOperationsTest.IconSize", "512" ) );
 		chanid = Integer.parseInt( properties.getProperty( "MythServicesServiceProvider.GuideOperationsTest.ChannelId",
 				"2502" ) );
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.mythtv.services.api.v026.impl.GuideTemplate#getChannelIcon(int, int, int)}
 	 * .
 	 */
 	@Test
 	public void testGetChannelIcon() throws MythServiceApiRuntimeException {
 		ResponseEntity<String> res = operations.getChannelIcon( chanid, iconsize, iconsize, ETagInfo.createEmptyETag() );
 		Assert.assertNotNull( res.getBody() );
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.mythtv.services.api.v026.impl.GuideTemplate#getProgramDetails(int, org.joda.time.DateTime)}
 	 * .
 	 */
 	@Test
 	public void testGetProgramDetails() throws MythServiceApiRuntimeException {
 		// let's run getProgramGuide to get an actual program.
 		DateTime fourHours = now.plus( Period.hours( 4 ) );
 		ResponseEntity<ProgramGuideWrapper> guideWrapper = operations.getProgramGuide( now, fourHours, 0, 10, false, ETagInfo.createEmptyETag() );
 		List<ChannelInfo> channels = guideWrapper.getBody().getProgramGuide().getChannels();
 		Assert.assertNotNull( channels );
 		Assert.assertFalse( "No channels retuned", channels.isEmpty() );
 		ChannelInfo chan = channels.get( 0 );
 		List<Program> programs = chan.getPrograms();
 		Assert.assertFalse( "No programs retuned", programs.isEmpty() );
 		ResponseEntity<ProgramWrapper> p = operations.getProgramDetails( chan.getChannelId(), programs.get( 0 ).getStartTime(), ETagInfo.createEmptyETag() );
 		Assert.assertNotNull( "ProgramWrapper is null", p.getBody() );
 		Assert.assertNotNull( "Program is null", p.getBody().getProgram() );
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.mythtv.services.api.v026.impl.GuideTemplate#getProgramGuide(org.joda.time.DateTime, org.joda.time.DateTime, int, int, boolean)}
 	 * .
 	 */
 	@Test
 	public void testGetProgramGuide() throws MythServiceApiRuntimeException {
 		ResponseEntity<ProgramGuideWrapper> guide = operations.getProgramGuide( now, tomorrow, 0, 100, true, ETagInfo.createEmptyETag() );
 		Assert.assertNotNull( guide.getBody() );
 	}
 
 	/**
 	 * Test method for
 	 * {@link org.mythtv.services.api.v026.impl.GuideTemplate#getProgramGuide(org.joda.time.DateTime, org.joda.time.DateTime, int, int, boolean)}
 	 * .
 	 */
 	@Ignore( "Use only against a live 0.26 backend" )
	@Test
 	public void testGetProgramGuideWithEtag() throws MythServiceApiRuntimeException {
 		ETagInfo etag = ETagInfo.createEmptyETag();
 		ResponseEntity<ProgramGuideWrapper> guide = operations.getProgramGuide( now, tomorrow, 0, 100, true, etag );
 		Assert.assertNotNull( guide.getBody() );
 		// make sure that we now have the etag marked as a new one (retrieved
 		// from previous call)
 		Assert.assertTrue( etag.isNewDataEtag() );
 		guide = operations.getProgramGuide( now, tomorrow, 0, 100, true, etag );
 		// etag should be the same
 		Assert.assertFalse( etag.isNewDataEtag() );
 		// because of the etag matching no data will be returned.
 		Assert.assertNull( guide.getBody() );
 	}
 	
 }
