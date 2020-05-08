 package net.praqma.hudson.test;
 
 import java.util.List;
 
 import org.hamcrest.CoreMatchers;
 import org.hamcrest.core.IsNot;
 import org.hamcrest.core.IsNull;
 
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.TagException;
 import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
 import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
 import net.praqma.clearcase.exceptions.UnableToGetEntityException;
 import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
 import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Tag;
 import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.hudson.CCUCMBuildAction;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 
 import static org.junit.Assert.*;
 
 import static org.hamcrest.CoreMatchers.*;
 
 public class SystemValidator {
 
 	private AbstractBuild<?, ?> build;
 	
 	public SystemValidator( AbstractBuild<?, ?> build ) {
 		this.build = build;
 	}
 	
 	public SystemValidator validate() throws ClearCaseException {
 		
 		System.out.println( "[Validating " + build + "]" );
 		
 		/* Jenkins build */
 		if( checkBuild ) {
 			System.out.println( "[Validating build]" );
 			checkBuild();
 		}
 
 		/* Built baseline not found */
 		if( checkBuiltBaselineNotFound ) {
 			System.out.println( "[Validating built baseline not found]" );
 			checkBuiltBaselineNotFound();
 		}
 		
 		/* Built baseline */
 		if( checkBuiltBaseline ) {
 			System.out.println( "[Validating built baseline]" );
 			checkBuiltBaseline();
 		}
 		
 		/* Check tagged baseline */
 		if( checkTagOnBuiltBaseline ) {
 			System.out.println( "[Validating tag on built baseline]" );
 			checkBaselineTag();
 		}
 		
 		/* Created baseline */
 		if( checkCreatedBaseline ) {
 			System.out.println( "[Validating created baseline]" );
 			checkCreatedBaseline();
 		}
 		
 		/**/
 		
 		return this;
 	}
 	
 	/* Validate build */
 	private boolean checkBuild = false;
 	private Result buildResult;
 	
 	public SystemValidator validateBuild( Result buildResult ) {
 		this.buildResult = buildResult;
 		this.checkBuild = true;
 		
 		return this;
 	}
 	
 	private void checkBuild() {
 		System.out.println( "[assert] " + "Jenkins build must be " + buildResult );
 		assertThat( build.getResult(), is( buildResult ) );
 	}
 	
 	/* Validate no built baseline */
 	private boolean checkBuiltBaselineNotFound = false;
 	
 	public SystemValidator validateBuiltBaselineNotFound() {
		 this.checkBuiltBaselineNotFound = true;
 		 
 		 return this;
 	}
 	
 	public void checkBuiltBaselineNotFound() {
 		Baseline baseline = getBuiltBaseline();
 		
 		System.out.println( "[assert] " + "Built baseline must bue null" );
 		assertNull( baseline );
 	}
 	
 	/* Validate build baseline */
 	private boolean checkBuiltBaseline = false;
 	private PromotionLevel builtBaselineLevel;
 	private Baseline expectedBuiltBaseline;
 	private Boolean builtBaselineIsRecommended;
 	
 	public SystemValidator validateBuiltBaseline( PromotionLevel level, Baseline expected ) {
 		return validateBuiltBaseline( builtBaselineLevel, expected, null );
 	}
 	
 	public SystemValidator validateBuiltBaseline( PromotionLevel level, Baseline expected, Boolean isRecommended ) {
 		this.checkBuiltBaseline = true;
 		this.expectedBuiltBaseline = expected;
 		this.builtBaselineLevel = level;
 		this.builtBaselineIsRecommended = isRecommended;
 		
 		return this;
 	}
 	
 	private void checkBuiltBaseline() throws ClearCaseException {
 		Baseline baseline = getBuiltBaseline();
 		assertNotNull( baseline );
 		baseline.load();
 		
 		/* Check level */
 		System.out.println( "[assert] " + baseline.getNormalizedName() + " must have the promotion level " + builtBaselineLevel );
 		assertEquals( builtBaselineLevel, baseline.getPromotionLevel( true ) );
 		
 		/* Check expected */
 		System.out.println( "[assert] " + baseline.getNormalizedName() + " must be the same as " + expectedBuiltBaseline.getNormalizedName() );
 		assertThat( baseline, is( expectedBuiltBaseline ) );
 		
 		/* Check recommendation */
 		if( builtBaselineIsRecommended != null ) {
 			System.out.println( "[assert] " + baseline.getNormalizedName() + " must " + (builtBaselineIsRecommended?"":"not ") + "be recommended" );
 			Stream stream = getStream().load();
 			List<Baseline> rbls = stream.getRecommendedBaselines();
 			assertEquals( 1, rbls.size() );
 			if( builtBaselineIsRecommended ) {
 				assertThat( baseline, is( rbls.get( 0 ) ) );
 			} else {
 				assertThat( baseline, not( rbls.get( 0 ) ) );
 			}
 		}
 	}
 	
 	private boolean checkTagOnBuiltBaseline = false;
 	private Baseline taggedBaseline;
 	private boolean baselineMustBeTagged = false;
 	
 	public SystemValidator validateBaselineTag( Baseline baseline, boolean mustBeTagged ) {
 		checkTagOnBuiltBaseline = true;
 		this.taggedBaseline = baseline;
 		this.baselineMustBeTagged = mustBeTagged;
 		
 		return this;
 	}
 	
 	public void checkBaselineTag() {
 
 		Tag tag;
 		try {
 			System.out.println( "[assert] " + taggedBaseline.getNormalizedName() + " must " + (baselineMustBeTagged?" ":"not ") + "be tagged" );
 			tag = Tag.getTag( taggedBaseline, build.getParent().getDisplayName(), build.getNumber()+"", false );
 			assertNotNull( tag );
 		} catch( Exception e ) {
 			fail( "Checking tag failed: " + e.getMessage() );
 		}
 	}
 
 	
 	
 	
 	/* Validate created baseline */
 	private boolean checkCreatedBaseline = false;
 	private Boolean createdBaselineExists;
 	
 	public SystemValidator validateCreatedBaseline( boolean exists ) {
 		this.checkCreatedBaseline = true;
 		this.createdBaselineExists = exists;
 		
 		return this;
 	}
 	
 	private void checkCreatedBaseline() throws ClearCaseException {
 		Baseline baseline = getCreatedBaseline();
 		
 		System.out.println( "Validating created baseline" );
 		
 		/* Validate null check */
 		if( createdBaselineExists != null ) {
 			System.out.println( "[assert] Created baseline must " + (createdBaselineExists?"not ": "") + "be null" );
 			if( createdBaselineExists ) {
 				assertNotNull( baseline );
 			} else {
 				assertNull( baseline );
 			}
 		}
 		
 	}
 	
 	/* Helpers */
 	private CCUCMBuildAction action;
 	
 	private CCUCMBuildAction getBuildAction() {
 		if( action == null ) {
 			action = build.getAction( CCUCMBuildAction.class );
 			assertNotNull( action );
 		}
 		
 		return action;
 	}
 	
 	private Baseline getBuiltBaseline() {
 		return getBuildAction().getBaseline();
 	}
 	
 	private Stream getStream() {
 		return getBuildAction().getStream();
 	}
 	
 	private Baseline getCreatedBaseline() {
 		return getBuildAction().getCreatedBaseline();
 	}
 	
 	
 }
