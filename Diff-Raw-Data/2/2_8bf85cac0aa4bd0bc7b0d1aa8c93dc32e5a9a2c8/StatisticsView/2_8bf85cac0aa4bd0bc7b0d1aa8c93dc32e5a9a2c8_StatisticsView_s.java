 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.statistics;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import net.grinder.common.GrinderException;
 
 
 /**
 * An ordered collection of {@link ExpressionViews}.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 public final class StatisticsView implements Externalizable 
 {
     private static final long serialVersionUID = -4846650473903375223L;
 
     /**
      * We define a <code>Comparator</code> for {@link ExpressionView}s
      * rather than having the <code>ExpressionView</code> implement
      * <code>Comparable</code> because our sort order is inconsistent with equals.
      **/
     private final static Comparator s_expressionViewComparator =
 	new Comparator() {
 	    public final int compare(Object a, Object b) {
 		final ExpressionView viewA = (ExpressionView)a;
 		final ExpressionView viewB = (ExpressionView)b;
 
 		if (viewA.getCreationOrder() < viewB.getCreationOrder()) {
 		    return -1;
 		}
 		else if (viewA.getCreationOrder() > viewB.getCreationOrder()) {
 		    return 1;
 		}
 		else {
 		    // Should assert ? Same creation order => same instance.
 		    return 0;
 		}
 	    }
 	};
 
     static
     {
 	// Ensure that the standard ExpressionViews are initialised
 	// before any user ExpressionViews.
 	Class dummy = CommonStatisticsViews.class;
     }
 
     /**
      * We use this set to ensure that new views are unique. We can't
      * do this with a SortedSet because our sort order is inconsistent
      * with equals.
      **/
     private final transient Set m_unique = new HashSet();
 
     /**
      * @link aggregation
      * @associates <{net.grinder.statistics.ExpressionView}>
      * @supplierCardinality 0..*
      **/
     private final SortedSet m_columns;
 
     /**
      * Creates a new <code>StatisticsView</code> instance.
      **/
     public StatisticsView()
     {
 	m_columns = new TreeSet(s_expressionViewComparator);
     }
 
     /**
      * Add all the {@link ExpressionView}s in <code>other</code> to
      * this <code>StatisticsView</code>.
      *
      * @param other Another <code>StatisticsView</code>.
      **/
     public final synchronized void add(StatisticsView other)
     {
 	final Iterator iterator = other.m_columns.iterator();
 
 	while (iterator.hasNext()) {
 	    add((ExpressionView)iterator.next());
 	}
     }
 
     /**
      * Add the specified {@link ExpressionView} to this
      * <code>StatisticsView</code>.
      *
      * @param statistic An {@link ExpressionView}.
      **/
     public final synchronized void add(ExpressionView statistic)
     {
 	if (!m_unique.contains(statistic)) {
 	    m_unique.add(statistic);
 	    m_columns.add(statistic);
 	}
     }
 
     /**
      * Return our {@link ExpressionView}s as an array.
      *
      * @return The {@link ExpressionView}s.
      **/
     public final synchronized ExpressionView[] getExpressionViews()
     {
 	return (ExpressionView[])m_columns.toArray(new ExpressionView[0]);
     }
 
     /**
      * Externalisation method.
      *
      * @param out Handle to the output stream.
      * @exception IOException If an I/O error occurs.
      **/
     public synchronized void writeExternal(ObjectOutput out) throws IOException
     {
 	out.writeInt(m_columns.size());
 
 	final Iterator iterator = m_columns.iterator();
 
 	while (iterator.hasNext()) {
 	    final ExpressionView view = (ExpressionView)iterator.next();
 	    view.myWriteExternal(out);
 	}
     }
 
     /**
      * Externalisation method.
      *
      * @param in Handle to the input stream.
      * @exception IOException If an I/O error occurs.
      **/
     public synchronized void readExternal(ObjectInput in) throws IOException
     {
 	final int n = in.readInt();
 
 	m_columns.clear();
 
 	for (int i=0; i<n; i++) {
 	    try {
 		add(new ExpressionView(in));
 	    }
 	    catch (GrinderException e) {
 		throw new IOException(
 		    "Could not instantiate ExpressionView: " + e.getMessage());
 	    }
 	}
     }
 }
