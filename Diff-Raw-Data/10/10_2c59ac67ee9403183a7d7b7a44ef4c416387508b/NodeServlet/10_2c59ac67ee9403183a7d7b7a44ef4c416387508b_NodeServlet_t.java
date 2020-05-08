 package cz.hack.zoorilla;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.curator.framework.CuratorFramework;
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.data.Stat;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 /**
  *
  * @author Phantom
  */
 public class NodeServlet extends HttpServlet {
 	
 	private final CuratorFramework client;
 
 	public NodeServlet(CuratorFramework curatorFramework) {
 		this.client = curatorFramework;
 	}
 	
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         try {
 			String nodePath = Path.fromRequest(req);
 			Stat stat = new Stat();
             client.getChildren().storingStatIn(stat).forPath(nodePath);
             byte[] nodeData = client.getData().forPath(nodePath);
             resp.setStatus(HttpServletResponse.SC_OK);
             resp.setHeader("X-Zoo-Version", String.valueOf(stat.getVersion()));
             resp.getOutputStream().write(nodeData);
             resp.getOutputStream().flush();
 		} catch(IllegalArgumentException ex) {
 			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
         } catch (KeeperException.NoNodeException ex) {
             resp.sendError(HttpServletResponse.SC_NOT_FOUND);
         } catch (Exception ex)  {
             throw new ServletException(ex);
         }
     }
     
     @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     	CreateMode mode = this.getCreateMode(req);
     	if(mode == null) {
     		resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
     		return;
     	}
     	try {
			String nodePath = Path.fromRequest(req);
			this.client.create().creatingParentsIfNeeded().withMode(mode).forPath(nodePath);
		} catch(IllegalArgumentException ex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
     	} catch (KeeperException.NodeExistsException e) {
     		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
 		} catch (Exception e) {
 			throw new ServletException(e);
 		}
     }
     
     private CreateMode getCreateMode(HttpServletRequest req) {
     	try {
 			JSONObject json = new JSONObject(new JSONTokener(req.getReader()));
 			return(CreateMode.valueOf(json.getString("type").toUpperCase()));
 		} catch (Exception e) {
 			return(null);
 		}
     }
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		try {
 			String nodePath = Path.fromRequest(req);
 			client.delete().forPath(nodePath);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch(IllegalArgumentException ex) {
 			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
 		} catch (KeeperException.NoNodeException ex) {
             resp.sendError(HttpServletResponse.SC_NOT_FOUND);
         } catch (Exception ex)  {
             throw new ServletException(ex);
         }
 	}
 	
 	
 }
