 /*
  * utils - package-info.java - Copyright © 2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * <h1>Usage</h1>
  *
  * <p>
  * The template system is used like this:
  * </p>
  *
  * <pre>
  * Template template = new Template(new FileReader("template.html"));
  * template.set("variable", "value");
  * template.set("items", Arrays.asList("foo", "bar", "baz"));
  * template.render(outputWriter);
  * </pre>
  *
  * <h2>Handling custom types</h2>
  *
  * <p>
  * The template system can be extended using
  * {@link net.pterodactylus.util.template.Accessor}s. An accessor is used to
  * allow template variable syntax like “object.foo”. Depending on the type of
  * {@code object}  the appropriate accessor is used to find the value of the
  * member “foo” (which can e.g. be retrieved by calling a complicated operation
  * on {@code object}).
  * </p>
  */
 package net.pterodactylus.util.template;
 
