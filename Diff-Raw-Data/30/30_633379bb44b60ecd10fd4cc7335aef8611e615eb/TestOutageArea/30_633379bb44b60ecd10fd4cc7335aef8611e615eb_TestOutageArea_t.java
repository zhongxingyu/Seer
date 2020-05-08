 package com.pocketcookies.pepco.model;
 
 import java.sql.Timestamp;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 import junit.framework.TestCase;
 
 public class TestOutageArea extends TestCase {
 	private SessionFactory sessionFactory;
 
 	public void setUp() {
 		this.sessionFactory = new Configuration().configure()
 				.buildSessionFactory();
 	}
 
 	public void testAreaRelationship() {
 		Session session = this.sessionFactory.getCurrentSession();
 		session.beginTransaction();
 		OutageArea area = new OutageArea("00000");
 		ParserRun run = new ParserRun(new Timestamp(1));
 		OutageAreaRevision r1 = new OutageAreaRevision(area, 1, run);
 		OutageAreaRevision r2 = new OutageAreaRevision(area, 1, run);
 		OutageAreaRevision r3 = new OutageAreaRevision(area, 1, run);
 		session.save(area);
 		session.save(run);
 		session.save(r1);
 		session.save(r2);
 		session.save(r3);
 		session.getTransaction().commit();
 		session = sessionFactory.getCurrentSession();
 		session.beginTransaction();
 		area = (OutageArea) session.load(OutageArea.class, area.getId());
 		run = (ParserRun) session.load(ParserRun.class, run.getId());
 		r1 = (OutageAreaRevision) session.load(OutageAreaRevision.class,
 				r1.getId());
 		r2 = (OutageAreaRevision) session.load(OutageAreaRevision.class,
 				r2.getId());
 		r3 = (OutageAreaRevision) session.load(OutageAreaRevision.class,
 				r3.getId());
 		assertTrue(area.getRevisions().contains(r1));
 		assertTrue(area.getRevisions().contains(r2));
 		assertTrue(area.getRevisions().contains(r3));
 
 		assertEquals(area, r1.getArea());
 		assertEquals(area, r2.getArea());
 		assertEquals(area, r3.getArea());
 
 		assertTrue(run.getAreaRevisions().contains(r1));
 		assertTrue(run.getAreaRevisions().contains(r2));
 		assertTrue(run.getAreaRevisions().contains(r3));
 
 		assertEquals(run, r1.getParserRun());
 		assertEquals(run, r2.getParserRun());
 		assertEquals(run, r3.getParserRun());
 	}
 
 	public void testComparison() {
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(1)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(2)))) < 0);
 		
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(2)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(1)))) > 0);
 		
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(1)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(1)))) == 0);
 		
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(1)))
 				.compareTo(
 				new OutageAreaRevision(1, null, 0, new ParserRun(new Timestamp(1)))) < 0);
 		
 		assertTrue(
 				new OutageAreaRevision(1, null, 0, new ParserRun(new Timestamp(1)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(1)))) > 0);
 		
 		//Test year 2038 problem by using timestamps >= 2^33.
 		
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(8589934592L)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(8589934592L))))== 0);
 		
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(8589934592L)))
 				.compareTo(
				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(17179869184L))))< 0);
 		
 		assertTrue(
				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(17179869184L)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(8589934592L))))> 0);
 		
 		assertTrue(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(8589934592L)))
 				.compareTo(
 				new OutageAreaRevision(1, null, 0, new ParserRun(new Timestamp(8589934592L))))< 0);
 		
 		assertTrue(
 				new OutageAreaRevision(1, null, 0, new ParserRun(new Timestamp(8589934592L)))
 				.compareTo(
 				new OutageAreaRevision(0, null, 0, new ParserRun(new Timestamp(8589934592L))))> 0);
 		}
 
 }
