 /*
  * Feb 9, 2006
  */
 package com.thinkparity.ophelia.model.io.db.hsqldb;
 
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Calendar;
 import java.util.TimeZone;
 import java.util.UUID;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.JVMUniqueId;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.email.EMailFormatException;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.jabber.JabberIdBuilder;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 import com.thinkparity.codebase.model.artifact.ArtifactFlag;
 import com.thinkparity.codebase.model.artifact.ArtifactState;
 import com.thinkparity.codebase.model.artifact.ArtifactType;
 import com.thinkparity.codebase.model.user.UserVCard;
 import com.thinkparity.codebase.model.util.xstream.XStreamUtil;
 
 import com.thinkparity.ophelia.model.audit.AuditEventType;
 import com.thinkparity.ophelia.model.container.ContainerDraft;
 import com.thinkparity.ophelia.model.io.md.MetaData;
 import com.thinkparity.ophelia.model.io.md.MetaDataType;
 import com.thinkparity.ophelia.model.message.SystemMessageType;
 
 /**
  * <b>Title:</b>thinkParity Hypersonic Session<br>
  * <b>Description:</b>A thinkParity hypersonic database session. A wrapper
  * around establishing a connection; creation of cached query statements; as
  * well as statement variable injection and result parameter extraction.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class Session {
 
     /** An apache logger wrapper. */
     protected static final Log4JWrapper LOGGER;
 
     /** An <code>XStreamUtil</code> instance. */
     protected static final XStreamUtil XSTREAM_UTIL;
 
     /** The local <code>TimeZone</code>. */
     private static final TimeZone TIME_ZONE;
 
     /** The universal <code>TimeZone</code>. */
     private static final TimeZone UNIVERSAL_TIME_ZONE;
 
     static {
         LOGGER = new Log4JWrapper("SQL_DEBUGGER");
         TIME_ZONE = TimeZone.getDefault();
         UNIVERSAL_TIME_ZONE = TimeZone.getTimeZone("Universal");
         XSTREAM_UTIL = XStreamUtil.getInstance();
     }
 
 	/** The sql connection. */
 	private final Connection connection;
 
     /** The session id. */
 	private final JVMUniqueId id;
 
     /** The connection meta data. */
     private DatabaseMetaData metaData;
 
     /** The session's prepared statement. */
 	private PreparedStatement preparedStatement;
 
 	/** The prepared statement's result set. */
 	private ResultSet resultSet;
 
     /** The <code>SessionManager</code>. */
     private final SessionManager sessionManager;
 
     /**
      * Create a Session.
      * 
      * @param sessionManager
      *            The <code>SessionManager</code>.
      * @param connection
      *            The sql <code>Connection</code>.
      */
 	Session(final SessionManager sessionManager, final Connection connection) {
 		super();
 		this.connection = connection;
 		this.id = JVMUniqueId.nextId();
         this.sessionManager = sessionManager;
 	}
 
     /**
      * Close the session.
      *
      */
     public void close() {
         assertConnectionIsOpen();
         try {
             close(preparedStatement, resultSet);
         } finally {
             try {
                 connection.close();
             } catch (final SQLException sqlx) {
                 throw panic(sqlx);
             } finally {
                 sessionManager.close(this);
             }
         }
     }
 
     /**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 * 
 	 */
 	public boolean equals(Object obj) {
 		if(null != obj && obj instanceof Session)
 			return id.equals(((Session) obj).id);
 		return false;
 	}
 
     /**
      * Execute sql.
      * 
      * @param sql
      *            An sql <code>String</code>.
      */
     public void execute(final String sql) {
         assertConnectionIsOpen();
         logStatement(sql);
         try {
             final Statement s = connection.createStatement();
             s.execute(sql);
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
     /**
      * Execute a sequence of sql.
      * 
      * @param sql
      *            An sql <code>String[]</code>.
      */
     public void execute(final String[] sql) {
         assertConnectionIsOpen();
         logStatements(sql);
         Statement statement = null;
         try {
             statement = connection.createStatement();
             for(final String s : sql) {
                 statement.addBatch(s);
             }
             statement.executeBatch();
         }
         catch(final SQLException sqlx) { throw panic(sqlx); }
         finally { close(statement); }
     }
 
     /**
      * Execute the SQL query in the session's prepared statement and set the
      * result set.
      * 
      */
     public void executeQuery() {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         try {
             resultSet = preparedStatement.executeQuery();
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
 	/**
      * Execute the SQL query in the session's prepared statement and set the
      * result set. Return the number of rows updated.
      * 
      * @return The number of rows updated.
      */
     public int executeUpdate() {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         try {
             return preparedStatement.executeUpdate();
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
     /**
      * Obtain the blob input stream from the result.
      * 
      * @param columnName
      *            A column name <code>String</code>.
      * @return An <code>InputStream</code>.
      */
     public Blob getActualBlob(final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final Blob value = resultSet.getBlob(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value;
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
     public AuditEventType getAuditEventTypeFromInteger(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : AuditEventType.fromId(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     /**
      * Obtain the blob input stream from the result.
      * 
      * @param columnName
      *            A column name <code>String</code>.
      * @return An <code>InputStream</code>.
      */
     public InputStream getBlob(final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final Blob value = resultSet.getBlob(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value.getBinaryStream();
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
     public Boolean getBoolean(final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final Boolean value = resultSet.getBoolean(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value;
         }
         catch(final SQLException sqlx) { throw new HypersonicException(sqlx); }
     }
 
     public byte[] getBytes(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final byte[] value = resultSet.getBytes(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value;
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public Calendar getCalendar(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
			final Calendar universalCalendar = DateUtil.getInstance();
            universalCalendar.setTimeZone(UNIVERSAL_TIME_ZONE);
			final Timestamp timestamp = resultSet.getTimestamp(columnName, universalCalendar);
 			if (resultSet.wasNull()) {
                 logColumnExtraction(columnName, null);
                 return null;
 			}
 			else {
                final Calendar localCalendar = DateUtil.getInstance();
                localCalendar.setTimeZone(TIME_ZONE);
                 localCalendar.setTimeInMillis(timestamp.getTime());
                 logColumnExtraction(columnName, localCalendar);
 				return localCalendar;
 			}
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public ContainerDraft.ArtifactState getContainerStateFromString(
             final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final String value = resultSet.getString(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ContainerDraft.ArtifactState.valueOf(value);
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 	public EMail getEMail(final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final String value = resultSet.getString(columnName);
             if (resultSet.wasNull()) {
                 logColumnExtraction(columnName, null);
                 return null;
             } else {
                 logColumnExtraction(columnName, value);
                 return EMailBuilder.parse(value);
             }
         } catch (final EMailFormatException efx) {
             throw new HypersonicException(efx);
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
     public ArtifactFlag getFlagFromInteger(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ArtifactFlag.fromId(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     
     public ArtifactFlag getFlagFromString(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final String value = resultSet.getString(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ArtifactFlag.valueOf(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     /**
 	 * Obtain the session id.
 	 * 
 	 * @return The session id.
 	 */
 	public JVMUniqueId getId() { return id; }
 
     /**
      * Execute a query to obtain the identity created.
      * 
      * @return The identity value.
      */
     public Long getIdentity(final String table) {
         assertConnectionIsOpen();
         final String sql = new StringBuffer("select IDENTITY_VAL_LOCAL() from ")
             .append(table)
             .toString();
         logStatement(sql);
         ResultSet resultSet = null;
         Statement statement = null;
         try {
             statement = connection.createStatement();
             resultSet = statement.executeQuery(sql);
             if (resultSet.next()) {
                 return resultSet.getLong(1);
             } else {
                 return null;
             }
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         } finally {
             close(statement, resultSet);
         }
     }
 
     /**
      * Obtain the input stream from the result.
      * 
      * @param columnName
      *            The column name.
      * @return An input stream.
      * @see ResultSet#getBinaryStream(String)
      */
 //    public InputStream getInputStream(final String columnName) {
 //        assertConnectionIsOpen();
 //        assertResultSetIsSet();
 //        try {
 //            final InputStream value = resultSet.getBinaryStream(columnName);
 //            logColumnExtraction(columnName, value);
 //            return resultSet.wasNull() ? null : value;
 //        } catch (final SQLException sqlx) {
 //            throw new HypersonicException(sqlx);
 //        }
 //    }
 
     public Integer getInteger(final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value;
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
     public Long getLong(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Long value = resultSet.getLong(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value;
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
 	}
 
     public void getMetaDataTables() {
         assertConnectionIsOpen();
         assertMetaDataIsSet();
         try {
             resultSet = metaData.getTables(null, "SA", null, new String[] {"TABLE"});
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
     public MetaDataType getMetaDataTypeFromInteger(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : MetaDataType.fromId(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public JabberId getQualifiedUsername(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
 			final String value = resultSet.getString(columnName);
 			if (resultSet.wasNull()) {
                 logColumnExtraction(columnName, null);
                 return null;
 			} else {
                 logColumnExtraction(columnName, value);
                 return JabberIdBuilder.parse(value);
 			}
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		} catch (final IllegalArgumentException iax) {
             throw new HypersonicException(iax);
 		}
 	}
 
 	public ArtifactState getStateFromInteger(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ArtifactState.fromId(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public ArtifactState getStateFromString(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final String value = resultSet.getString(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ArtifactState.valueOf(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public String getString(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final String value = resultSet.getString(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : value;
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public SystemMessageType getSystemMessageTypeFromInteger(
 			final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : SystemMessageType.fromId(value);
 		} catch(final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public ArtifactType getTypeFromInteger(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final Integer value = resultSet.getInt(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ArtifactType.fromId(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public ArtifactType getTypeFromString(final String columnName) {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             final String value = resultSet.getString(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : ArtifactType.valueOf(value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public UUID getUniqueId(final String columnName) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final String value = resultSet.getString(columnName);
             logColumnExtraction(columnName, value);
             return resultSet.wasNull() ? null : UUID.fromString(value);
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
 	public <T extends UserVCard> T getVCard(final String columnName,
             final T vcard) {
         assertConnectionIsOpen();
         assertResultSetIsSet();
         try {
             final String vcardXML = resultSet.getString(columnName);
             if (resultSet.wasNull()) {
                 logColumnExtraction(columnName, null);
                 return null;
             }
             else {
                 logColumnExtraction(columnName, vcardXML);
                 final StringReader vcardXMLReader = new StringReader(vcardXML);
                 XSTREAM_UTIL.fromXML(vcardXMLReader, vcard);
                 return vcard;
             }
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
 
     }
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 * 
 	 */
 	public int hashCode() {
 		return id.hashCode();
 	}
 
     public boolean nextResult() {
 		assertConnectionIsOpen();
 		assertResultSetIsSet();
 		try {
             return resultSet.next();
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	/**
 	 * Obtain the database metadata.
 	 * 
 	 * @return The database metadata.
 	 * @throws HypersonicException
 	 */
 	public void openMetaData() {
 		assertConnectionIsOpen();
 		try {
             metaData = connection.getMetaData();
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public PreparedStatement prepareStatement(final String sql) {
 		assertConnectionIsOpen();
 		logStatement(sql);
 		try {
 			preparedStatement = connection.prepareStatement(sql);
 			return preparedStatement;
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     /**
      * Set a blob column value.
      * 
      * @param index
      *            The column index.
      * @param value
      *            The column value.
      * @param bufferSize
      *            The size of the buffer to use when writing the value.
      */
     public void setBinaryStream(final Integer index, final InputStream value,
             final Long length, final Integer bufferSize) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setBinaryStream(index, value, length.intValue());
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
     public void setBoolean(final Integer index, final Boolean value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setBoolean(index, value);
         } catch(final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 	public void setBytes(final Integer index, final byte[] value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setBytes(index, value);
 		} catch(final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setCalendar(final Integer index, final Calendar value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             final Calendar universalCalendar = (Calendar) value.clone();
             universalCalendar.setTimeZone(UNIVERSAL_TIME_ZONE);
 			preparedStatement.setTimestamp(index,
 					new Timestamp(universalCalendar.getTimeInMillis()), universalCalendar);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setEMail(final Integer index, final EMail value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setString(index, value.toString());
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 	public void setEnumTypeAsString(final Integer index, final Enum<?> value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setString(index, value.toString());
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 	public void setFlagAsInteger(final Integer index, final ArtifactFlag value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setInt(index, value.getId());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public void setFlagAsString(final Integer index, final ArtifactFlag value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public void setInt(final Integer index, final Integer value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setInt(index, value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setLong(final Integer index, final Long value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setLong(index, value);
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public void setMetaDataAsString(final Integer index, final MetaData value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public void setQualifiedUsername(final Integer index, final JabberId value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.getQualifiedUsername());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setStateAsInteger(final Integer index, final ArtifactState value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setInt(index, value.getId());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setStateAsString(final Integer index, final ArtifactState value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setString(index, value.toString());
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
     public void setStateAsString(final Integer index, final ContainerDraft.ArtifactState value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setString(index, value.toString());
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 //    public void setStream(final Integer index, final InputStream value,
 //            final Long valueLength) {
 //        assertConnectionIsOpen();
 //        assertPreparedStatementIsSet();
 //        logColumnInjection(index, value, valueLength);
 //        try {
 //            preparedStatement.setBinaryStream(index, value, valueLength.intValue());
 //        } catch (final SQLException sqlx) {
 //            throw new HypersonicException(sqlx);
 //        }
 //    }
 
 	public void setString(final Integer index, final String value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             if (null == value) {
                 preparedStatement.setNull(index, Types.VARCHAR);
             } else {
                 preparedStatement.setString(index, value);
             }
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setTypeAsInteger(final Integer index, final ArtifactType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setInt(index, value.getId());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setTypeAsInteger(final Integer index, final AuditEventType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setInt(index, value.getId());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setTypeAsInteger(final Integer index, final MetaDataType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setInt(index, value.getId());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setTypeAsInteger(final Integer index, final SystemMessageType value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             preparedStatement.setInt(index, value.getId());
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 	public void setTypeAsString(final Integer index, final ArtifactType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
 	}
 
     public void setTypeAsString(final Integer index, final AuditEventType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setTypeAsString(final Integer index, final Enum<?> value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 
 	public void setTypeAsString(final Integer index, final MetaDataType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
 	public void setTypeAsString(final Integer index, final SystemMessageType value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public void setUniqueId(final Integer index, final UUID value) {
 		assertConnectionIsOpen();
 		assertPreparedStatementIsSet();
         logColumnInjection(index, value);
 		try {
             preparedStatement.setString(index, value.toString());
 		} catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
 		}
 	}
 
     public <T extends UserVCard> void setVCard(final Integer index,
             final T value) {
         assertConnectionIsOpen();
         assertPreparedStatementIsSet();
         logColumnInjection(index, value);
         try {
             final StringWriter valueWriter = new StringWriter();
             XSTREAM_UTIL.toXML(value, valueWriter);
             preparedStatement.setString(index, valueWriter.toString());
         } catch (final SQLException sqlx) {
             throw new HypersonicException(sqlx);
         }
     }
 
 	/**
      * Assert the connection is open.
      *
      */
     private void assertConnectionIsOpen() {
         try {
             if (null == connection)
                 throw new HypersonicException("Connection is null.");
             if (connection.isClosed())
                 throw new HypersonicException("Connection is closed.");
         } catch (final SQLException sqlx) {
             panic(sqlx);
         }
     }
 
     /**
      * Assert the meta data is set.
      *
      */
 	private void assertMetaDataIsSet() {
         if (null == metaData)
             throw new HypersonicException("Meta data is null.");
     }
 
 	/**
      * Assert the prepared statement is set.
      *
      */
     private void assertPreparedStatementIsSet() {
         if (null == preparedStatement)
             throw new HypersonicException("Prepared statement is null.");
     }
 
     /**
      * Assert the result set is set.
      *
      */
     private void assertResultSetIsSet() {
         if (null == resultSet)
             throw new HypersonicException("Result set is null.");
     }
 
 
     /**
      * Close a result set.
      * 
      * @param resultSet
      *            A <code>ResultSet</code>
      */
     private void close(final ResultSet resultSet) {
         try {
             if (null != resultSet)
                 resultSet.close();
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
     /**
      * Close a statement.
      * 
      * @param statement
      *            A <code>Statement</code>.
      */
     private void close(final Statement statement) {
         try {
             if(null != statement)
                 statement.close();
         } catch (final SQLException sqlx) {
             throw panic(sqlx);
         }
     }
 
     /**
      * Close a statement as well as a result set.
      * 
      * @param statement
      *            A <code>Statement</code>.
      * @param resultSet
      *            A <code>ResultSet</code>.
      * 
      * @see HypersonicSession#close(Statement)
      * @see HypersonicSession#close(ResultSet)
      */
     private void close(final Statement statement, final ResultSet resultSet) {
         close(statement);
         close(resultSet);
     }
 
     /**
      * Log the column name and value.
      * 
      * @param columnName
      *            The column name <code>String</code>.
      * @param columnValue
      *            The column value <code>Object</code>.
      */
     private void logColumnExtraction(final String columnName, final Object columnValue) {
         LOGGER.logDebug("Extract {0}:{1}", columnName, columnValue);
     }
 
     /**
      * Log the column name and value.
      * 
      * @param columnName
      *            The column name <code>String</code>.
      * @param columnValue
      *            The column value <code>Object</code>.
      */
     private void logColumnInjection(final Integer index, final Object columnValue) {
         LOGGER.logDebug("Inject {0}:{1}", index, columnValue);
     }
 
     /**
      * Log the column name, value and extra data.
      * 
      * @param index
      *            The column index <code>Integer</code>.
      * @param columnValue
      *            The column value <code>Object</code>.
      * @param columnValueData
      *            The column value data <code>Object</code>.
      */
 //    private void logColumnInjection(final Integer index,
 //            final Object columnValue, final Object columnValueData) {
 //        LOGGER.logDebug("Inject {0}:{1} - {2}", index, columnValue, columnValueData);
 //    }
 
     /**
      * Log an sql statement.
      * 
      * @param sql
      *            An sql statement <code>String</code>.
      */
     private void logStatement(final String sql) {
         LOGGER.logDebug("Statement:{0}", sql);
     }
 
     /**
      * Log a sequence of sql statements.
      * 
      * @param sql
      *            An sql statement <code>String[]</code>.
      */
     private void logStatements(final String[] sql) {
         for(final String s : sql)
             logStatement(s);
     }
 
     /**
      * Panic. Create a hypersonic runtime exception.
      * 
      * @param t
      *            A <code>Throwable</code>.
      */
     private HypersonicException panic(final Throwable t) {
         if (HypersonicException.class.isAssignableFrom(t.getClass())) {
             return (HypersonicException) t;
         } else {
             return new HypersonicException(t);
         }
     }
 }
