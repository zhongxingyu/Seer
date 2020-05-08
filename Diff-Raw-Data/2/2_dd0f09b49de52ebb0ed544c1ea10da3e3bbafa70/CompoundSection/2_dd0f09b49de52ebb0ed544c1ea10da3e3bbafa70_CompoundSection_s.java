 /* Copyright (c) 2008-2010 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz.section;
 
 import org.fiz.*;
 import java.util.*;
 
 /**
  * A CompoundSection is a Section that contains one or more other Sections.
  * By default the sections will be output right after each other, but if a
  * {@code layout} is specified, that is used to arrange the sections.
  * CompoundSections support the following constructor properties:
  *   background:     (optional) Specifies a background color to use for
  *                   the interior of this section (everything inside the
  *                   border).  Defaults to transparent.
  *   borderFamily:   (optional) Specifies the base name for a family of
  *                   images that will be used to display a border around this
  *                   section.  If this option has the value {@code x.gif}, then
  *                   there must exist images named {@code x-nw.gif},
  *                   {@code x-n.gif}, {@code x-ne.gif}, {@code x-e.gif},
  *                   {@code x-se.gif}, {@code x-s.gif}, {@code x-sw.gif},
  *                   and {@code x-w.gif}; {@code x-nw.gif} displays the
  *                   upper left corner of the border, {@code x-n.gif} will be
  *                   stretched to cover the top of the border, and so on.
  *   layout:         (optional) Specifies a layout for arranging child
  *                   sections. See {@link Layout} for details. The layout will
  *                   be passed in a Dataset with id-section pairs, with the id
  *                   being the id of the section.
  *   class:          (optional) Used as the {@code class} attribute for
  *                   the HTML table or div that contains the CompoundSection.
  *   id:             (optional)  Used as the {@code id} attribute for the
  *                   HTML table or div that contains the CompoundSection.
  *                   Used to find the section in Javascript, e.g. to make it
  *                   visible or invisible.  Must be unique among all id's
  *                   for the page.
  *
  * CompoundSection automatically sets the following {@code class} attributes
  * for use in CSS:
  *   compoundBody:   The {@code <tr>} element containing all of the nested
  *                   child sections.
  */
 public class CompoundSection extends Section {
     // The following variables are copies of the constructor arguments by
     // the same names.  See the constructor documentation for details.
     protected Section[] children;
 
     // If the {@code add} method has been called, the following variable
     // keeps track of all of the additional children (not including
     // those already referred to by the {@code children} variable).
     protected ArrayList<Section> extraChildren = null;
 
     /**
      * Construct a CompoundSection.
      * @param properties           Contains configuration information for
      *                             the section.
      * @param children             Any number of Sections, which will be
      *                             displayed inside this section
      */
     public CompoundSection(Dataset properties, Section ... children) {
         this.properties = properties;
         this.children = children;
     }
 
     /**
      * Add one or more additional children to an existing compound section.
      * The new children go at the end of the list.
      * @param children             One or more additional children.
      */
     public void add(Section ... children) {
         if (extraChildren == null) {
             extraChildren = new ArrayList<Section>();
         }
         for (Section child: children) {
             extraChildren.add(child);
         }
     }
 
     @Override
     public void render(ClientRequest cr) {
         // If there is a border for this section, then the section gets
         // rendered as a 3x3 table, with the outer cells containing the
         // border and the inner cell containing the children's sections.
         // If there is no border then the section is rendered in a <div>.
 
         // Render the portion of the container that comes before the children.
         StringBuilder out = cr.getHtml().getBody();
         Template.appendHtml(out, "\n<!-- Start CompoundSection {{@id}} -->\n",
                 properties);
         String borderFamily = properties.checkString("borderFamily");
         Layout layout = null;
        if (properties.check("layout") instanceof Layout) {
             layout = (Layout) properties.get("layout");
         }
         if (borderFamily != null) {
             Template.appendHtml(out, "<table {{id=\"@id\"}} {{class=\"@class\"}} " +
                     "cellspacing=\"0\">\n" +
                     "  <tr style=\"line-height: 0px;\">\n" +
                     "    <td><img src=\"@1\" alt=\"\" />" +
                     "</td>\n" +
                     "    <td style=\"background-image: " +
                     "url(@2); " +
                     "background-repeat: repeat-x;\">" +
                     "</td>\n" +
                     "    <td><img src=\"@3\" alt=\"\" />" +
                     "</td>\n" +
                     "  </tr>\n" +
                     "  <tr>\n" +
                     "    <td style=\"background-image: " +
                     "url(@4); " +
                     "background-repeat: repeat-y;\">" +
                     "</td>\n" +
                     "    <td class=\"compoundBody\" " +
                     "{{style=\"background: @background;\"}}>\n",
                                 properties, StringUtil.addSuffix(borderFamily, "-nw"),
                                 StringUtil.addSuffix(borderFamily, "-n"),
                                 StringUtil.addSuffix(borderFamily, "-ne"),
                                 StringUtil.addSuffix(borderFamily, "-w"));
         } else {
             Template.appendHtml(out, "<div {{id=\"@id\"}} {{class=\"@class\"}} " +
                     "{{style=\"background: @background;\"}}>\n",
                     properties);
         }
 
         if (layout != null) {
             Dataset data = new Dataset();
             for (Section child : children) {
                 data.set(child.getId(), child);
             }
             if (extraChildren != null) {
                 for (Section child : extraChildren) {
                     data.set(child.getId(), child);
                 }
             }
             layout.render(cr, data);
         } else {
             for (Section child: children) {
                 child.render(cr);
             }
             if (extraChildren != null) {
                 for (Section child: extraChildren) {
                     child.render(cr);
                 }
             }
         }
 
         // Render the portion of the container that comes after the children.
         if (borderFamily != null) {
             Template.appendHtml(out, "    </td>\n" +
                     "    <td style=\"background-image: " +
                     "url(@1); " +
                     "background-repeat: repeat-y;\">" +
                     "</td>\n" +
                     "  </tr>\n" +
                     "  <tr style=\"line-height: 0px;\">\n" +
                     "    <td><img src=\"@2\" alt=\"\" />" +
                     "</td>\n" +
                     "    <td style=\"background-image: " +
                     "url(@3); " +
                     "background-repeat: repeat-x;\">" +
                     "</td>\n" +
                     "    <td><img src=\"@4\" alt=\"\" />" +
                     "</td>\n" +
                     "  </tr>\n" +
                     "</table>\n", properties,
                     StringUtil.addSuffix(borderFamily, "-e"),
                     StringUtil.addSuffix(borderFamily, "-sw"),
                     StringUtil.addSuffix(borderFamily, "-s"),
                     StringUtil.addSuffix(borderFamily, "-se"));
         } else {
             out.append("</div>\n");
         }
         Template.appendHtml(out, "<!-- End CompoundSection {{@id}} -->\n",
                 properties);
     }
 
     /**
      * Generate HTML for a child element of this container and append it to
      * the Html object associated with {@code cr}. If a child element
      * named {@code id} does not exist, this method just returns without
      * modifying {@code cr}.
      *
      * @param id The id of the child element.
      * @param cr Overall information about the client
      *           request being serviced; HTML should get appended to
      *           {@code cr.getHtml()}.
      */
     public void renderChild(String id, ClientRequest cr) {
         // Search for a child Section whose id is "id".
         for (Section child : children) {
             String childId = child.checkId();
             if (childId != null && childId.equals(id)) {
                 // Found a child section with a matching id, generate Html
                 // and return.
                 child.render(cr);
                 return;
             }
         }
         if (extraChildren != null) {
             for (Section child : extraChildren) {
                 String childId = child.checkId();
                 if (childId != null && childId.equals(id)) {
                     child.render(cr);
                     return;
                 }
             }
         }
     }
 }
