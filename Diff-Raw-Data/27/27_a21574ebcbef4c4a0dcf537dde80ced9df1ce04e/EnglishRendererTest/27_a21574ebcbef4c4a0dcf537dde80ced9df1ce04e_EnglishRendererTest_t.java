 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Imibare.
  *
  * Imibare is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Imibare is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Imibare. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.imibare.test.renderer;
 
import java.io.IOException;

 import junit.framework.TestCase;
 
 import com.ijuru.imibare.renderer.EnglishRenderer;
import com.ijuru.imibare.test.TestUtils;
 
 /**
  * Test case for EnglishRenderer class
  */
 public class EnglishRendererTest extends TestCase {
 
	public void testRender() throws IOException {
		TestUtils.runCases("/data-english.csv", new EnglishRenderer());
 	}
 }
