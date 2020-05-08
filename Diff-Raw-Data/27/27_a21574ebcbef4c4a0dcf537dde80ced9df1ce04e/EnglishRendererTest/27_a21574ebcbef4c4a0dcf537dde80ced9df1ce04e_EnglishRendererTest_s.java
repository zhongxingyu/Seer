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
 
 import junit.framework.TestCase;
 
 import com.ijuru.imibare.renderer.EnglishRenderer;
import com.ijuru.imibare.renderer.Renderer;
 
 /**
  * Test case for EnglishRenderer class
  */
 public class EnglishRendererTest extends TestCase {
 
	public void testRender() {
		Renderer renderer = new EnglishRenderer();
		assertEquals("zero", renderer.render(0, 0));
		assertEquals("one", renderer.render(1, 0));
		assertEquals("fifteen", renderer.render(15, 0));
		assertEquals("twenty three", renderer.render(23, 0));
		assertEquals("one hundred and three", renderer.render(103, 0));
		assertEquals("one hundred and forty five", renderer.render(145, 0));
		assertEquals("seven thousand six hundred and ninety two", renderer.render(7692, 0));
		assertEquals("eight million forty seven thousand six hundred and ninety two", renderer.render(8047692, 0));
		assertEquals("negative forteen", renderer.render(-14, 0));
		assertEquals("negative one thousand and sixty five", renderer.render(-1065, 0));
 	}
 }
