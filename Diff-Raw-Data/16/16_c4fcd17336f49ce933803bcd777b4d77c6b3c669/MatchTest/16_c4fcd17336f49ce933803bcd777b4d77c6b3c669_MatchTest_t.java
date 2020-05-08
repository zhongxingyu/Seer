 /*
  * dfh.grammar -- a recursive descent parser library for Java
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.grammar;
 
import java.io.Serializable;

 /**
  * For testing whether a {@link Match} meets some arbitrary test.
  * <p>
  * <b>Creation date:</b> Mar 29, 2011
  * 
  * @author David Houghton
  * 
  */
public interface MatchTest extends Serializable {
 	/**
 	 * Maximally flexible test prototype.
 	 * 
 	 * @param m
 	 *            the {@link Match} that must pass the test
 	 * @return whether the {@link Match} passes
 	 */
 	boolean test(Match m);
 }
