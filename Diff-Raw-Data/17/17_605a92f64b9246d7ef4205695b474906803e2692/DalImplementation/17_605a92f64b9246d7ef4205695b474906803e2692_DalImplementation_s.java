 package hu.documaison.dal.interfaces;
 
 import hu.documaison.dal.database.DatabaseUtils;
 import hu.documaison.data.entities.*;
 import hu.documaison.data.exceptions.InvalidParameterException;
 import hu.documaison.data.exceptions.UnableToCreateException;
 import hu.documaison.data.exceptions.UnknownDocumentException;
 import hu.documaison.data.exceptions.UnknownDocumentTypeException;
 import hu.documaison.data.exceptions.UnknownTagException;
 import hu.documaison.data.helper.DataHelper;
 import hu.documaison.data.helper.DocumentFilePointer;
 import hu.documaison.data.helper.MetadataNameTypePair;
 import hu.documaison.data.search.BoolOperator;
 import hu.documaison.data.search.Expression;
 import hu.documaison.data.search.SearchExpression;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.stmt.DeleteBuilder;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.Where;
 import com.j256.ormlite.support.ConnectionSource;
 
 class DalImplementation implements DalInterface {
 
 	private <T extends DatabaseObject> T genericCreate(Class<T> c, String info) {
 		T instance;
 		try {
 			instance = c.newInstance();
 		} catch (InstantiationException e) {
 			return null;
 		} catch (IllegalAccessException e) {
 			return null;
 		}
 		return genericCreate(c, instance, info);
 	}
 
 	private <T extends DatabaseObject> T genericCreate(Class<T> c, T instance,
 			String info) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<T, Integer> dao = DaoManager.createDao(connectionSource, c);
 
 			// add
 			T ret = instance;
 			dao.create(ret);
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, info);
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, info);
 				}
 			}
 		}
 		return null;
 	}
 
 	private <T extends DatabaseObject> void genericDelete(int id, Class<T> c,
 			String info) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<T, Integer> dao = DaoManager.createDao(connectionSource, c);
 
 			// delete
 			dao.deleteById(id);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, info);
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, info);
 				}
 			}
 		}
 	}
 
 	private <T extends DatabaseObject> void genericUpdate(T entity, Class<T> c,
 			String info) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<T, Integer> dao = DaoManager.createDao(connectionSource, c);
 
 			// update
 			dao.update(entity);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, info);
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, info);
 				}
 			}
 		}
 	}
 
 	@Override
 	public Comment createComment() throws UnableToCreateException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Comment, Integer> dao = DaoManager.createDao(connectionSource,
 					Comment.class);
 
 			// add
 			Comment comment = new Comment();
 			dao.create(comment);
 			return comment;
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "createComment");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "createComment");
 				}
 			}
 		}
 
 		throw new UnableToCreateException("Comment");
 	}
 
 	@Override
 	public DefaultMetadata createDefaultMetadata()
 			throws UnableToCreateException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DefaultMetadata, Integer> dao = DaoManager.createDao(
 					connectionSource, DefaultMetadata.class);
 
 			// add
 			DefaultMetadata ret = new DefaultMetadata();
 			dao.create(ret);
 			return ret;
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "createDefaultMetadata");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "createDefaultMetadata");
 				}
 			}
 		}
 
 		throw new UnableToCreateException("DefaultMetadata");
 	}
 
 	@Override
 	public Document createDocument(int typeId)
 			throws UnknownDocumentTypeException, UnableToCreateException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			DocumentType documentType = this.getDocumentType(typeId);
 			if (documentType == null) {
 				throw new UnknownDocumentTypeException(typeId);
 			}
 
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// add
 			Document newDocument = new Document();
 			newDocument.setType(documentType);
 			dao.create(newDocument);
 
 			// set properties
 
 			// following lines moved to BLL
 			// if (documentType.getDefaultMetadataCollection() != null) {
 			// for (DefaultMetadata md : documentType
 			// .getDefaultMetadataCollection()) {
 			// newDocument.addMetadata(md.createMetadata());
 			// }
 			// }
 			// if (documentType.getDefaultThumbnailBytes() != null) {
 			// newDocument.setThumbnailBytes(documentType
 			// .getDefaultThumbnailBytes().clone());
 			// }
 
 			newDocument.setDateAdded(new Date());
 			dao.update(newDocument);
 
 			return newDocument;
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "createDocument");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "createDocument");
 				}
 			}
 		}
 
 		throw new UnableToCreateException("Document");
 	}
 
 	@Override
 	public DocumentType createDocumentType() throws UnableToCreateException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentType, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentType.class);
 
 			// add
 			DocumentType documentType = new DocumentType(dao);
 			dao.create(documentType);
 			return documentType;
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "createDocumentType");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "createDocumentType");
 				}
 			}
 		}
 
 		throw new UnableToCreateException("DocumentType");
 	}
 
 	@Override
 	public Metadata createMetadata() throws UnableToCreateException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Metadata, Integer> dao = DaoManager.createDao(connectionSource,
 					Metadata.class);
 
 			// add
 			Metadata ret = new Metadata();
 			dao.create(ret);
 			return ret;
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "createMetadata");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "createMetadata");
 				}
 			}
 		}
 
 		throw new UnableToCreateException("Metadata");
 	}
 
 	@Override
 	public Tag createTag(String name) {
 		return genericCreate(Tag.class, "createTag");
 	}
 
 	@Override
 	public Document getDocument(int id) throws UnknownDocumentException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// query
 			Document ret = dao.queryForId(id);
 			if (ret == null) {
 				throw new UnknownDocumentException(id);
 			}
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocument");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocument");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	// @Override
 	// public void addTag(Tag tag) {
 	// // create a connection source to our database
 	// ConnectionSource connectionSource = null;
 	//
 	// try {
 	// // create connection
 	// connectionSource = DatabaseUtils.getConnectionSource();
 	//
 	// // instantiate the dao
 	// Dao<Tag, Integer> dao = DaoManager.createDao(connectionSource,
 	// Tag.class);
 	//
 	// // add
 	// dao.create(tag);
 	//
 	// } catch (SQLException e) {
 	// HandleSQLException(e, "addTag");
 	// } finally {
 	// // close connection
 	// if (connectionSource != null) {
 	// try {
 	// connectionSource.close();
 	// } catch (SQLException e) {
 	// HandleSQLException(e, "addTag");
 	// }
 	// }
 	// }
 	// }
 
 	@Override
 	public Collection<Document> getDocuments() {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// query
 			List<Document> ret = dao.queryForAll();
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocuments");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocuments");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Collection<Document> getDocumentsByTags(List<Tag> tags) {
 		if (tags == null) {
 			return new ArrayList<Document>();
 		}
 
 		ArrayList<Integer> tagIds = new ArrayList<Integer>();
 		for (Tag t : tags) {
 			tagIds.add(t.getId());
 		}
 		return getDocumentsByTagIds(tagIds);
 	}
 
 	@Override
 	public Collection<Document> getDocumentsByTag(Tag tag) {
 		return getDocumentsByTagId(tag.getId());
 	}
 
 	private Collection<Document> getDocumentsByTagId(int tagId) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			Dao<DocumentTagConnection, Integer> daoTags = DaoManager.createDao(
 					connectionSource, DocumentTagConnection.class);
 
 			// query
 			QueryBuilder<Document, Integer> qb = dao.queryBuilder();
 			QueryBuilder<DocumentTagConnection, Integer> qbTags = daoTags
 					.queryBuilder();
 
 			qbTags.where().eq(DocumentTagConnection.TAGID, tagId);
 			qb.join(qbTags);
 			System.out.println("Query: " + qb.prepareStatementString());
 			List<Document> ret = dao.query(qb.prepare());
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocumentsByTagId(int)");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocumentsByTagId(int)");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private Collection<Document> getDocumentsByTagIds(List<Integer> tagIds) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			Dao<DocumentTagConnection, Integer> daoTags = DaoManager.createDao(
 					connectionSource, DocumentTagConnection.class);
 
 			// query
 			QueryBuilder<Document, Integer> qb = dao.queryBuilder();
 			QueryBuilder<DocumentTagConnection, Integer> qbTags = daoTags
 					.queryBuilder();
 
 			qbTags.where().in(DocumentTagConnection.TAGID, tagIds);
 			qb.join(qbTags);
 			qb.distinct();
 			System.out.println("Query: " + qb.prepareStatementString());
 			List<Document> ret = dao.query(qb.prepare());
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocumentsByTagId(int)");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocumentsByTagId(int)");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public DocumentType getDocumentType(int id)
 			throws UnknownDocumentTypeException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentType, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentType.class);
 
 			// query
 			DocumentType ret = dao.queryForId(id);
 			if (ret == null) {
 				throw new UnknownDocumentTypeException(id);
 			}
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocumentType");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocumentType");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Collection<DocumentType> getDocumentTypes() {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentType, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentType.class);
 
 			// query
 			List<DocumentType> ret = dao.queryForAll();
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocumentTypes");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocumentTypes");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Tag getTag(int id) throws UnknownTagException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Tag, Integer> dao = DaoManager.createDao(connectionSource,
 					Tag.class);
 
 			// query
 			Tag ret = dao.queryForId(id);
 			if (ret == null) {
 				throw new UnknownTagException(id);
 			}
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getTags");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getTags");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	// @Override
 	// public void addDocumentType(DocumentType documentType) {
 	// // create a connection source to our database
 	// ConnectionSource connectionSource = null;
 	//
 	// try {
 	// // create connection
 	// connectionSource = DatabaseUtils.getConnectionSource();
 	//
 	// // instantiate the dao
 	// Dao<DocumentType, Integer> dao = DaoManager.createDao(
 	// connectionSource, DocumentType.class);
 	//
 	// // add
 	// dao.create(documentType);
 	//
 	// } catch (SQLException e) {
 	// HandleSQLException(e, "addDocumentType");
 	// } finally {
 	// // close connection
 	// if (connectionSource != null) {
 	// try {
 	// connectionSource.close();
 	// } catch (SQLException e) {
 	// HandleSQLException(e, "addDocumentType");
 	// }
 	// }
 	// }
 	// }
 
 	@Override
 	public Tag getTag(String name) throws UnknownTagException {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Tag, Integer> dao = DaoManager.createDao(connectionSource,
 					Tag.class);
 
 			// query
 			Tag ret = dao.queryForFirst(dao.queryBuilder().where()
 					.eq(Tag.NAME, name).prepare());
 			if (ret == null) {
 				throw new UnknownTagException(0);
 			}
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getTag");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getTag");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Collection<Tag> getTags() {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Tag, Integer> dao = DaoManager.createDao(connectionSource,
 					Tag.class);
 
 			// query
 			List<Tag> ret = dao.queryForAll();
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getTags");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getTags");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private void HandleSQLException(SQLException e, String method) {
 		System.err.println("Exception @ " + method);
 		e.printStackTrace();
 	}
 
 	@Override
 	public void removeComment(int id) {
 		genericDelete(id, Comment.class, "removeComment");
 	}
 
 	@Override
 	public void removeDefaultMetadata(int id) {
 		genericDelete(id, DefaultMetadata.class, "removeDefaultMetadata");
 	}
 
 	@Override
 	public void removeDocument(int id) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// delete
 			dao.deleteById(id);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "removeDocument");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "removeDocument");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void removeDocumentType(int id) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentType, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentType.class);
 
 			// add
 			dao.deleteById(id);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "removeDocumentType");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "removeDocumentType");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void removeMetadata(int id) {
 		genericDelete(id, Metadata.class, "removeMetadata");
 	}
 
 	@Override
 	public void removeTag(int id) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Tag, Integer> dao = DaoManager.createDao(connectionSource,
 					Tag.class);
 
 			// delete
 			dao.deleteById(id);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "removeTag");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "removeTag");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void updateComment(Comment comment) {
 		genericUpdate(comment, Comment.class, "updateComment");
 	}
 
 	@Override
 	public void updateDefaultMetadata(DefaultMetadata metadata) {
 		genericUpdate(metadata, DefaultMetadata.class, "updateDefaultMetadata");
 	}
 
 	@Override
 	public void updateDocument(Document document) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// update
 			dao.update(document);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "updateDocument");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "updateDocument");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void updateDocumentType(DocumentType documentType) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentType, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentType.class);
 
 			// update
 			dao.update(documentType);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "updateDocumentType");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "updateDocumentType");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void updateMetadata(Metadata metadata) {
 		genericUpdate(metadata, Metadata.class, "updateMetadata");
 
 	}
 
 	@Override
 	public void updateTag(Tag tag) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Tag, Integer> dao = DaoManager.createDao(connectionSource,
 					Tag.class);
 
 			// update
 			dao.update(tag);
 
 		} catch (SQLException e) {
 			HandleSQLException(e, "updateTag");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "updateTag");
 				}
 			}
 		}
 	}
 
 	@Override
 	public Collection<Document> searchDocuments(SearchExpression sexpr) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// instantiate the metadata dao
 			Dao<Metadata, Integer> daoMD = DaoManager.createDao(
 					connectionSource, Metadata.class);
 
 			// query
 			QueryBuilder<Document, Integer> qb = dao.queryBuilder();
 			QueryBuilder<Metadata, Integer> qbMD = daoMD.queryBuilder();
 
 			qb.join(qbMD);
 
 			// TODO: review
 			Where<Metadata, Integer> where = qbMD.where();
 			Boolean notFirst = false;
 			for (Expression expr : sexpr.getExpressions()) {
 				// Expression-k sszefzse
 				if (notFirst) {
 					if (sexpr.getBoolOperator() == BoolOperator.or) {
 						where.or();
 					} else {
 						where.and();
 					}
 				}
 
 				qbMD.where().eq(AbstractMetadata.NAME, expr.getMetadataName());
 				switch (expr.getOperator()) {
 				case eq:
 					where.and().eq(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				case ge:
 					where.and().ge(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				case gt:
 					where.and().gt(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				case contains:
 					where.and().like(AbstractMetadata.VALUE,
 							"%" + expr.getValue() + "%");
 					break;
 				case le:
 					where.and().le(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				case like:
 					where.and().like(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				case lt:
 					where.and().lt(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				case neq:
 					where.and().ne(AbstractMetadata.VALUE, expr.getValue());
 					break;
 				default:
 
 					break;
 				}
 
 				notFirst = true;
 			}
 
 			System.out.println("Search for: " + where.getStatement()); // TODO:
 																		// delete
 			List<Document> ret = dao.query(qb.prepare());
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "searchDocuments(SearchExpression)");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "searchDocuments(SearchExpression)");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public void addTagToDocument(Tag tag, Document document)
 			throws InvalidParameterException {
 		// parameter check
 		if (tag == null || !DataHelper.isValidId(tag.getId())) {
 			throw new InvalidParameterException("tag");
 		}
 		if (document == null) {
 			throw new InvalidParameterException("document");
 		}
 
 		DocumentTagConnection dtc = new DocumentTagConnection();
 		dtc.setDocument(document);
 		dtc.setTag(tag);
 
 		dtc = genericCreate(DocumentTagConnection.class, dtc,
 				"addTagToDocument");
 
 		genericUpdate(dtc, DocumentTagConnection.class, "addTagToDocument");
 	}
 
 	@Override
 	public void removeTagFromDocument(Tag tag, Document document)
 			throws InvalidParameterException {
 		// parameter check
 		if (tag == null || !DataHelper.isValidId(tag.getId())) {
 			throw new InvalidParameterException("tag");
 		}
 		if (document == null) {
 			throw new InvalidParameterException("document");
 		}
 
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentTagConnection, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentTagConnection.class);
 
 			// query
 			DeleteBuilder<DocumentTagConnection, Integer> db = dao
 					.deleteBuilder();
 			db.where().eq(DocumentTagConnection.DOCUMENTID, document.getId())
 					.and().eq(DocumentTagConnection.TAGID, tag.getId());
 			dao.delete(db.prepare());
 		} catch (SQLException e) {
 			HandleSQLException(e, "removeTagFromDocument");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "removeTagFromDocument");
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param locationFilter
 	 *            Used in SQL where clause. Use % as joker character. If the
 	 *            value is null, all items will be returned.
 	 */
 	@Override
 	public Collection<DocumentFilePointer> getDocumentPointers(
 			String locationFilter) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 
 			// query
 			QueryBuilder<Document, Integer> qb = dao.queryBuilder();
 			if (locationFilter != null) {
 				qb.where().like(Document.LOCATION, locationFilter);
 			}
 
 			List<Document> documents = dao.query(qb.prepare());
 
 			List<DocumentFilePointer> ret = new ArrayList<DocumentFilePointer>();
 			for (Document doc : documents) {
 				if (doc.getLocation() != null) {
 					ret.add(DocumentFilePointer.createInstance(doc));
 				}
 			}
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocumentPointers(String)");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocumentPointers(String)");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public DocumentType getDocumentTypeForExtension(String extension) {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<DocumentType, Integer> dao = DaoManager.createDao(
 					connectionSource, DocumentType.class);
 
 			// query
 			QueryBuilder<DocumentType, Integer> qb = dao.queryBuilder();
 			qb.where().like(DocumentType.DEFAULTEXTS, "%" + extension + "%");
 			// TODO: kezelni azt, hogy ez pl. a doc-ra visszaadja a docx-et
 			// is!!!
 
 			List<DocumentType> ret = dao.query(qb.prepare());
 
 			// return
 			if (ret.size() == 0) {
 				return null;
 			} else {
 				return ret.get(0);
 			}
 		} catch (SQLException e) {
 			HandleSQLException(e, "getDocumentTypes");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getDocumentTypes");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Collection<Document> searchDocumentsFreeText(String textFragment) {
 		if (textFragment == null) {
 			return new ArrayList<Document>();
 		}
 
 		String expr = "%" + textFragment.replace('%', '_') + "%";
 
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Document, Integer> dao = DaoManager.createDao(connectionSource,
 					Document.class);
 			// instantiate the metadata dao
 			Dao<Metadata, Integer> daoMD = DaoManager.createDao(
 					connectionSource, Metadata.class);
 
 			// query
 			QueryBuilder<Document, Integer> qb = dao.queryBuilder();
 			QueryBuilder<Metadata, Integer> qbMD = daoMD.queryBuilder();
 			QueryBuilder<Document, Integer> joinedQb = qb.join(qbMD);
 
 			Where<Metadata, Integer> where = qbMD.where();
 			where.like(AbstractMetadata.VALUE, expr);
 			// qb.where().like(Document.LOCATION, expr);
 
 			// System.out.println("Search for: " + where.getStatement());
 
 			List<Document> ret = dao.query(joinedQb.prepare());
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "searchDocumentsFreeText(String)");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "searchDocumentsFreeText(String)");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private Collection<Metadata> getAllMetadata() {
 		// create a connection source to our database
 		ConnectionSource connectionSource = null;
 
 		try {
 			// create connection
 			connectionSource = DatabaseUtils.getConnectionSource();
 
 			// instantiate the dao
 			Dao<Metadata, Integer> dao = DaoManager.createDao(connectionSource,
 					Metadata.class);
 
 			// query
 			List<Metadata> ret = dao.queryForAll();
 
 			// return
 			return ret;
 		} catch (SQLException e) {
 			HandleSQLException(e, "getAllMetadata");
 		} finally {
 			// close connection
 			if (connectionSource != null) {
 				try {
 					connectionSource.close();
 				} catch (SQLException e) {
 					HandleSQLException(e, "getAllMetadata");
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Collection<MetadataNameTypePair> getAllMetadataKeys() {
 		HashSet<MetadataNameTypePair> set = new HashSet<MetadataNameTypePair>();
 
 		for (Metadata md : getAllMetadata()) {
 			MetadataNameTypePair pair = new MetadataNameTypePair(md.getName(),
 					md.getMetadataType());
 			set.add(pair);
 		}
 
 		return set;
 	}
 
 }
