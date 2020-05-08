 package com.bioinformaticsapp.test.functional;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.hamcrest.core.IsNull.notNullValue;
 
 import static com.bioinformaticsapp.test.testhelpers.BLASTQueryBuilder.*;
 import static com.bioinformaticsapp.models.BLASTQuery.Status;
 
 import java.util.List;
 
 import android.test.InstrumentationTestCase;
 
 import com.bioinformaticsapp.data.BLASTQueryLabBook;
 import com.bioinformaticsapp.models.BLASTQuery;
 import com.bioinformaticsapp.models.BLASTVendor;
 import com.bioinformaticsapp.test.testhelpers.OhBLASTItTestHelper;
 
 public class BLASTQueryLabBookTest extends InstrumentationTestCase {
 
 	private BLASTQueryLabBook labBook;
 	
 	public void setUp() throws Exception {
 		super.setUp();
 		OhBLASTItTestHelper helper = new OhBLASTItTestHelper(getInstrumentation().getTargetContext());
 		helper.cleanDatabase();
 		labBook = new BLASTQueryLabBook(getInstrumentation().getTargetContext());
 	}
 	
 	public void testWeCanSaveTheBLASTQueryToTheLabBook(){
 		BLASTQuery query = labBook.save(aBLASTQuery());
 		
 		assertThat("Saved BLAST query must have a primary key",
 				query.getPrimaryKey(), is(notNullValue()));
 	}
 	
 	public void testWeCanRetrieveAQueryByIdentifier(){
 		BLASTQuery query = labBook.save(aBLASTQuery());
 		
 		BLASTQuery fromStorage= labBook.findQueryById(query.getPrimaryKey());
 		
 		assertThat("Should be able to find the right query by ID", fromStorage, is(equalTo(query)));
 	}
 	
 	public void testWeCanEditAnExistingQuery(){
 		BLASTQuery query = labBook.save(aBLASTQuery());
 		query.setSearchParameter("email", "h.n.varambhia@gmail.com");
 		long primaryKey = query.getPrimaryKey();
 		
 		query = labBook.save(query);
 		
 		BLASTQuery fromStorage = labBook.findQueryById(primaryKey);
 		assertThat("Should store query changes to a BLASTquery", fromStorage, is(query));
 	}
 	
 	public void testWeCanRetrieveBLASTQueriesWithAStatus(){
 		BLASTQuery aQuery = labBook.save(aBLASTQueryWithStatus(Status.DRAFT));
 		BLASTQuery queryWithDifferentStatus = labBook.save(aBLASTQueryWithStatus(Status.FINISHED));
 		
 		List<BLASTQuery> drafts = labBook.findBLASTQueriesByStatus(Status.DRAFT);
 		
 		assertThat("Should be able to find BLAST queries by their status", drafts.contains(aQuery));
 		assertThat("Should not contain queries with a status different to the one required", !drafts.contains(queryWithDifferentStatus));
 	}
 	
 	public void testWeCanRetrievesNoPendingBLASTQueriesIfThereAreNone(){
 		List<BLASTQuery> pendingQueries = labBook.findPendingBLASTQueriesFor(BLASTVendor.NCBI);
 		
 		assertThat("list should not contain any pending blast queries for a supplier if there are none", pendingQueries.isEmpty());
 	}
 	
 	public void testWeCanRetrievePendingBLASTQueriesToBeSentToSupplier(){
 		BLASTQuery pending = labBook.save(aBLASTQueryWithStatusAndVendor(Status.PENDING, BLASTVendor.NCBI));
 		
 		List<BLASTQuery> pendingQueries = labBook.findPendingBLASTQueriesFor(BLASTVendor.NCBI);
 		
 		assertThat("list should contain pending blast queries for a supplier", pendingQueries.contains(pending));
 	}
 	
 	public void testWeCanRetrieveSubmittedBLASTQueriesToBeSentToSupplier(){
 		BLASTQuery submitted = labBook.save(aBLASTQueryWithStatusAndVendor(Status.SUBMITTED, BLASTVendor.NCBI));
 		
		List<BLASTQuery> submittedQueries = labBook.submittedBLASTQueriesForVentor(BLASTVendor.NCBI);
 		
 		assertThat("list should contain submitted blast queries for supplier", submittedQueries.contains(submitted));
 	}
 	
 	private BLASTQuery aBLASTQueryWithStatusAndVendor(Status status, int vendor){
 		BLASTQuery blastQuery = aBLASTQuery();
 		switch(vendor){
 		case BLASTVendor.EMBL_EBI:
 			blastQuery = BLASTQuery.emblBLASTQuery("blastn");
 			break;
 		case BLASTVendor.NCBI:
 			blastQuery = BLASTQuery.ncbiBLASTQuery("blastn");
 			break;
 		default:
 			blastQuery = aBLASTQuery();
 			break;
 		}
 		blastQuery.setStatus(status);
 		return blastQuery;
 	}
 
 }
