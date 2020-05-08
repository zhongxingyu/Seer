 package org.openrdf.sail.auditing;
 
 
 import javax.xml.datatype.DatatypeFactory;
 
 import junit.framework.TestCase;
 
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.trig.TriGWriter;
 import org.openrdf.sail.auditing.vocabulary.Audit;
 import org.openrdf.sail.memory.MemoryStore;
 
 public class AuditingTest extends TestCase {
 	private ValueFactory vf = ValueFactoryImpl.getInstance();
 	private String NS = "http://example.com/";
 	private URI carmichael = vf.createURI(NS, "carmichael");
 	private URI harris = vf.createURI(NS, "harris");
 	private URI jackson = vf.createURI(NS, "jackson");
 	private URI johnston = vf.createURI(NS, "johnston");
 	private URI lismer = vf.createURI(NS, "lismer");
 	private URI macDonald = vf.createURI(NS, "macDonald");
 	private URI varley = vf.createURI(NS, "varley");
 	private URI thomson = vf.createURI(NS, "thomson");
 	private URI knows = vf.createURI("http://xmlns.com/foaf/0.1/knows");
 
 	private RepositoryConnection reopen(Repository repo,
 			RepositoryConnection con) throws RepositoryException {
 		con.setAutoCommit(true);
 		con.close();
 		con = repo.getConnection();
 		con.setAutoCommit(false);
 		return con;
 	}
 
 	public void testAdd() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddMany() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(harris, knows, jackson);
 		con = reopen(repo, con);
 		con.add(jackson, knows, johnston);
 		con = reopen(repo, con);
 		con.add(johnston, knows, lismer);
 		con = reopen(repo, con);
 		con.add(lismer, knows, macDonald);
 		con = reopen(repo, con);
 		con.add(macDonald, knows, varley);
 		con = reopen(repo, con);
 		con.add(varley, knows, thomson);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(7, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemove() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		con.export(new TriGWriter(System.out));
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveAdd() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testReplace() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveEach() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, null, null, false);
 		while (stmts.hasNext()) {
 			con.remove(stmts.next());
 		}
 		stmts.close();
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveRevision() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveLastRevision() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, Audit.REVISION, null, false);
 		Value revision = stmts.next().getObject();
 		stmts.close();
 		con.remove(carmichael, Audit.REVISION, revision);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testTouchRevision() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testDoubleTouchRevision() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testClear() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, null, null);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddManyArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(harris, knows, jackson);
 		con = reopen(repo, con);
 		con.add(jackson, knows, johnston);
 		con = reopen(repo, con);
 		con.add(johnston, knows, lismer);
 		con = reopen(repo, con);
 		con.add(lismer, knows, macDonald);
 		con = reopen(repo, con);
 		con.add(macDonald, knows, varley);
 		con = reopen(repo, con);
 		con.add(varley, knows, thomson);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(7, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		assertEquals(2, con.getStatements(null, RDF.TYPE, Audit.RECENT, false).asList().size());
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveAddArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testReplaceArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveEachArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, null, null, false);
 		while (stmts.hasNext()) {
 			con.remove(stmts.next());
 		}
 		stmts.close();
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveRevisionArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveLastRevisionArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, Audit.REVISION, null, false);
 		Value revision = stmts.next().getObject();
 		stmts.close();
 		con.remove(carmichael, Audit.REVISION, revision);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testTouchRevisionArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testDoubleTouchRevisionArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testClearArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setArchiving(true);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, null, null);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddManyMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(harris, knows, jackson);
 		con = reopen(repo, con);
 		con.add(jackson, knows, johnston);
 		con = reopen(repo, con);
 		con.add(johnston, knows, lismer);
 		con = reopen(repo, con);
 		con.add(lismer, knows, macDonald);
 		con = reopen(repo, con);
 		con.add(macDonald, knows, varley);
 		con = reopen(repo, con);
 		con.add(varley, knows, thomson);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(7, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		assertEquals(2, con.getStatements(null, RDF.TYPE, Audit.RECENT, false).asList().size());
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveAddMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testReplaceMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveEachMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, null, null, false);
 		while (stmts.hasNext()) {
 			con.remove(stmts.next());
 		}
 		stmts.close();
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveRevisionMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveLastRevisionMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, Audit.REVISION, null, false);
 		Value revision = stmts.next().getObject();
 		stmts.close();
 		con.remove(carmichael, Audit.REVISION, revision);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testTouchRevisionMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testDoubleTouchRevisionMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testClearMaxArchiving() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, null, null);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddManyObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(harris, knows, jackson);
 		con = reopen(repo, con);
 		con.add(jackson, knows, johnston);
 		con = reopen(repo, con);
 		con.add(johnston, knows, lismer);
 		con = reopen(repo, con);
 		con.add(lismer, knows, macDonald);
 		con = reopen(repo, con);
 		con.add(macDonald, knows, varley);
 		con = reopen(repo, con);
 		con.add(varley, knows, thomson);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(7, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		assertEquals(2, con.getStatements(null, RDF.TYPE, Audit.RECENT, false).asList().size());
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveAddObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testReplaceObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveEachObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, null, null, false);
 		while (stmts.hasNext()) {
 			con.remove(stmts.next());
 		}
 		stmts.close();
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveRevisionObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveLastRevisionObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, Audit.REVISION, null, false);
 		Value revision = stmts.next().getObject();
 		stmts.close();
 		con.remove(carmichael, Audit.REVISION, revision);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testTouchRevisionObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testDoubleTouchRevisionObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(3, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testClearObsolete() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P30D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, null, null);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertFalse(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testAddManyPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(harris, knows, jackson);
 		con = reopen(repo, con);
 		con.add(jackson, knows, johnston);
 		con = reopen(repo, con);
 		con.add(johnston, knows, lismer);
 		con = reopen(repo, con);
 		con.add(lismer, knows, macDonald);
 		con = reopen(repo, con);
 		con.add(macDonald, knows, varley);
 		con = reopen(repo, con);
 		con.add(varley, knows, thomson);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertFalse(con.hasStatement(null, null, null, false, new Resource[]{null}));
 		assertEquals(7, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertFalse(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		assertEquals(2, con.getStatements(null, RDF.TYPE, Audit.RECENT, false).asList().size());
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemovePurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveAddPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertFalse(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testReplacePurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, knows, harris);
 		con.add(carmichael, knows, jackson);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, knows, jackson, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveEachPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, null, null, false);
 		while (stmts.hasNext()) {
 			con.remove(stmts.next());
 		}
 		stmts.close();
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveRevisionPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testRemoveLastRevisionPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		RepositoryResult<Statement> stmts = con.getStatements(carmichael, Audit.REVISION, null, false);
 		Value revision = stmts.next().getObject();
 		stmts.close();
 		con.remove(carmichael, Audit.REVISION, revision);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertFalse(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testTouchRevisionPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
 		assertFalse(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testDoubleTouchRevisionPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con = reopen(repo, con);
 		con.remove(carmichael, Audit.REVISION, null);
 		con.add(carmichael, Audit.REVISION, Audit.CURRENT_TRX);
 		con.setAutoCommit(true);
 		assertTrue(con.hasStatement(carmichael, knows, harris, false));
 		assertTrue(con.hasStatement(carmichael, Audit.REVISION, null, false));
 		assertEquals(2, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.export(new TriGWriter(System.out));
 		con.close();
 		repo.shutDown();
 	}
 
 	public void testClearPurge() throws Exception {
 		AuditingSail sail = new AuditingSail(new MemoryStore());
 		sail.setMaxArchive(2);
 		sail.setMinRecent(2);
 		sail.setMaxRecent(2);
 		sail.setPurgeAfter(DatatypeFactory.newInstance().newDuration("P0D"));
 		Repository repo = new SailRepository(sail);
 		repo.initialize();
 		RepositoryConnection con = repo.getConnection();
 		con.setAutoCommit(false);
 		assertTrue(con.isEmpty());
 		con.add(carmichael, knows, harris);
 		con = reopen(repo, con);
 		con.remove(carmichael, null, null);
 		con.setAutoCommit(true);
 		assertFalse(con.hasStatement(carmichael, null, null, false));
 		assertEquals(1, con.getContextIDs().asList().size());
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.TRANSACTION, false));
 		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.RECENT, false));
		assertTrue(con.hasStatement(null, RDF.TYPE, Audit.OBSOLETE, false));
 		assertTrue(con.hasStatement(null, Audit.COMMITTED_ON, null, false));
 		assertTrue(con.hasStatement(null, Audit.MODIFIED, null, false));
 		assertTrue(con.hasStatement(null, Audit.PREDECESSOR, null, false));
 		assertTrue(con.hasStatement(null, Audit.CONTAINED, null, false));
 		con.close();
 		repo.shutDown();
 	}
 
 }
