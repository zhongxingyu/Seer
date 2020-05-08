 /*
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.database;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Tuan Nguyen (tuan08@users.sourceforge.net)
  * @since Nov 25, 2004
  * @version $Id: ObjectQuery.java 6006 2006-06-06 10:01:27Z thangvn $
  */
 public class ObjectQuery
 {
 
    private static SimpleDateFormat ft_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 
    private Class type_;
 
    private String orderBy_;
 
    private String groupBy_;
 
    private List<Parameter> parameters_;
 
    private List<Parameter> selectParameter_;
 
    public ObjectQuery(Class type)
    {
       type_ = type;
       parameters_ = new ArrayList<Parameter>(3);
       selectParameter_ = new ArrayList<Parameter>(10);
    }
 
    public ObjectQuery addEQ(String field, Object value)
    {
       if (value != null)
       {
          parameters_.add(new Parameter(" = ", field, value));
       }
       return this;
    }
 
    public ObjectQuery addGT(String field, Object value)
    {
       if (value != null)
       {
          parameters_.add(new Parameter(" > ", field, value));
       }
       return this;
    }
 
    public ObjectQuery addLT(String field, Object value)
    {
       if (value != null)
       {
          parameters_.add(new Parameter(" < ", field, value));
       }
       return this;
    }
 
    public ObjectQuery addLIKE(String field, String value)
    {
       if (value != null && value.length() > 0)
       {
          parameters_.add(new Parameter(" LIKE ", field, optimizeInputString(value)));
       }
       return this;
    }
 
    public String optimizeInputString(String value)
    {
       value = value.replace('*', '%');
       value = value.replaceAll("'", "&#39;");
       value = value.replaceAll("<", "&#60;");
       value = value.replaceAll(">", "&#62;");
       return value;
    }
 
    public ObjectQuery addSUM(String field)
    {
       selectParameter_.add(new Parameter("SUM", field));
       return this;
    }
 
    public ObjectQuery addSelect(String field)
    {
       selectParameter_.add(new Parameter("FIELDSELECT", field));
       return this;
    }
 
    public ObjectQuery addSelectCount(String type)
    {
       selectParameter_.add(new Parameter("COUNTSELECT", type));
       return this;
    }
 
    public ObjectQuery addSelectMaxMin(String op, String field)
    {
       selectParameter_.add(new Parameter(op, field));
       return this;
    }
 
    public ObjectQuery setGroupBy(String field)
    {
       groupBy_ = " GROUP BY o." + field;
       return this;
    }
 
    public ObjectQuery setAscOrderBy(String field)
    {
       orderBy_ = " ORDER BY o." + field + " asc";
       return this;
    }
 
    public ObjectQuery setDescOrderBy(String field)
    {
       orderBy_ = " ORDER BY o." + field + " desc";
       return this;
    }
 
    public String getHibernateQuery()
    {
       StringBuffer b = new StringBuffer();
       b.append("from o in class ").append(type_.getName());
       if (parameters_.size() > 0)
       {
          b.append(" WHERE ");
          for (int i = 0; i < parameters_.size(); i++)
          {
             if (i > 0)
                b.append(" AND ");
             Parameter p = parameters_.get(i);
             if (p.value_ instanceof String)
             {
               if (p.field_.startsWith("UPPER") || p.field_.startsWith("LOWER"))
               {
                  b.append(p.field_).append(p.op_).append("'").append(p.value_).append("'");
               }
               else
               {
                  b.append(" o.").append(p.field_).append(p.op_).append("'").append(p.value_).append("'");
               }
             }
             else if (p.value_ instanceof Date)
             {
                String value = ft_.format((Date)p.value_);
                b.append(" o.").append(p.field_).append(p.op_).append("'").append(value).append("'");
             }
             else
             {
                b.append(" o.").append(p.field_).append(p.op_).append(p.value_);
             }
          }
       }
       if (orderBy_ != null)
          b.append(orderBy_);
       return b.toString();
    }
 
    public String getHibernateGroupByQuery()
    {
       StringBuffer b = new StringBuffer();
       b.append("select ");
       if (selectParameter_.size() > 0)
       {
          for (int i = 0; i < selectParameter_.size(); i++)
          {
             Parameter p = selectParameter_.get(i);
             if (p.op_.equals("fieldselect"))
             {
                b.append("o.").append(p.field_);
             }
             else if (p.op_.equals("countselect"))
             {
                b.append("COUNT");
                if (p.field_ != "" || p.field_.length() > 0)
                {
                   b.append("(").append(p.field_).append(" o)");
                }
                else
                {
                   b.append("(o)");
                }
             }
             else
             {
                b.append(p.op_).append("(").append("o.").append(p.field_).append(") ");
             }
             if (i < selectParameter_.size() - 1)
                b.append(" , ");
          }
       }
       b.append(" from o in class ").append(type_.getName());
       if (parameters_.size() > 0)
       {
          b.append(" where ");
          for (int i = 0; i < parameters_.size(); i++)
          {
             if (i > 0)
                b.append(" and ");
             Parameter p = parameters_.get(i);
             if (p.value_ instanceof String)
             {
                b.append(" o.").append(p.field_).append(p.op_).append("'").append(p.value_).append("'");
             }
             else if (p.value_ instanceof Date)
             {
                String value = ft_.format((Date)p.value_);
                b.append(" o.").append(p.field_).append(p.op_).append("'").append(value).append("'");
             }
             else if (p.op_.equals("MAX") || p.op_.equals("MIN"))
             {
                b.append(p.op_).append("(").append("o.").append(p.field_).append(") ");
             }
             else
             {
                b.append(" o.").append(p.field_).append(p.op_).append(p.value_);
             }
          }
       }
       if (groupBy_ != null)
          b.append(groupBy_);
       if (orderBy_ != null)
          b.append(orderBy_);
       return b.toString();
    }
 
    public String getHibernateCountQuery()
    {
       StringBuffer b = new StringBuffer();
       b.append("SELECT COUNT(o) FROM o IN CLASS ").append(type_.getName());
       if (parameters_.size() > 0)
       {
          b.append(" WHERE ");
          for (int i = 0; i < parameters_.size(); i++)
          {
             if (i > 0)
                b.append(" AND ");
             Parameter p = parameters_.get(i);
             if (p.value_ instanceof String)
             {
               if (p.field_.startsWith("UPPER") || p.field_.startsWith("LOWER"))
               {
                  b.append(p.field_).append(p.op_).append("'").append(p.value_).append("'");
               }
               else
               {
                  b.append(" o.").append(p.field_).append(p.op_).append("'").append(p.value_).append("'");
               }
             }
             else if (p.value_ instanceof Date)
             {
                String value = ft_.format((Date)p.value_);
                b.append(" o.").append(p.field_).append(p.op_).append("'").append(value).append("'");
             }
             else
             {
                b.append(" o.").append(p.field_).append(p.op_).append(p.value_);
             }
          }
       }
       return b.toString();
    }
 
    static class Parameter
    {
       String op_;
 
       String field_;
 
       String label_;
 
       Object value_;
 
       Parameter(String op, String field, Object value)
       {
          op_ = op;
          field_ = field;
          value_ = value;
       }
 
       Parameter(String op, String field)
       {
          op_ = op;
          field_ = field;
       }
    }
 }
